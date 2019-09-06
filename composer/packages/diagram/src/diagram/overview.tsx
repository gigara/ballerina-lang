import { ASTKindChecker, ASTNode, ASTUtil } from "@ballerina/ast-model";
import { BallerinaAST, IBallerinaLangClient, ProjectAST } from "@ballerina/lang-service";
import { PanZoom } from "panzoom";
import React from "react";
import { DropdownItemProps, ListItemProps } from "semantic-ui-react";
import { visitor as expandingResettingVisitor } from "../visitors/expandings-undoing-visitor";
import { visitor as initVisitor } from "../visitors/init-visitor";
import { getReachedInvocationDepth, setMaxInvocationDepth, setProjectAST, visitor as invocationVisitor
    } from "../visitors/invocation-expanding-visitor";
import { CommonDiagramProps, Diagram } from "./diagram";
import { DiagramMode } from "./diagram-context";
import { DiagramUtils } from "./diagram-utils";
import { TopMenu } from "./top-menu";

const modes = [
    {
        text: "Interaction",
        type: DiagramMode.INTERACTION
    },
    {
        text: "Statements",
        type: DiagramMode.STATEMENT
    },
];

export interface OverviewProps extends CommonDiagramProps {
    langClient: IBallerinaLangClient;
    initialSelectedModule?: string;
    initialSelectedConstruct?: ConstructIdentifier;
}
export interface OverviewState {
    modules: ProjectAST;
    selectedConstruct?: ConstructIdentifier | undefined;
    mode: DiagramMode;
    modeText: string;
    zoomFactor: number;
    openedState: boolean;
    maxInvocationDepth: number;
}

export interface ConstructIdentifier {
    constructName: string;
    moduleName: string;
    subConstructName?: string;
}

export class Overview extends React.Component<OverviewProps, OverviewState> {
    private panZoomComp: PanZoom | undefined;
    private panZoomElement: SVGGElement | undefined;
    private innitialPanZoomTransform: {
        x: number;
        y: number;
        scale: number;
    } | undefined;
    constructor(props: OverviewProps) {
        super(props);
        this.handleModeChange = this.handleModeChange.bind(this);
        this.handleConstructClick = this.handleConstructClick.bind(this);
        this.handleBackClick = this.handleBackClick.bind(this);
        this.handleFitClick = this.handleFitClick.bind(this);
        this.handleZoomIn = this.handleZoomIn.bind(this);
        this.handleZoomOut = this.handleZoomOut.bind(this);
        this.handleConstructNameSelect = this.handleConstructNameSelect.bind(this);
        this.handleModuleNameSelect = this.handleModuleNameSelect.bind(this);
        this.handleOpened = this.handleOpened.bind(this);
        this.handleClosed = this.handleClosed.bind(this);
        this.handleReset = this.handleReset.bind(this);
        this.setPanZoomComp = this.setPanZoomComp.bind(this);
        this.setMaxInvocationDepth = this.setMaxInvocationDepth.bind(this);
        this.state = {
            maxInvocationDepth: -1,
            mode: DiagramMode.INTERACTION,
            modeText: "Interaction",
            modules: {},
            openedState: false,
            zoomFactor: 1,
        };
    }

    public updateAST() {
        const { langClient, sourceRootUri, docUri } = this.props;
        if (sourceRootUri) {
            langClient.getProjectAST({ sourceRoot: sourceRootUri }).then((result) => {
                if (!result || !(Object.keys(result.modules).length > 0)) {
                    return;
                }
                this.setState({
                    modules: result.modules
                });
            }, () => {/** no op */});
        } else {
            langClient.getAST({documentIdentifier: {uri: docUri}}).then((result) => {
                const ast = result.ast as any;
                if (!ast) {
                    return;
                }

                this.setState({
                    modules: {
                        [ast.name]: {
                            compilationUnits: {
                                [ast.name]: {
                                    ast,
                                    name: ast.name,
                                    uri: docUri,
                                }
                            },
                            name: ast.name,
                        }
                    }
                });
            });
        }
    }

    public selectConstruct({moduleName, constructName, subConstructName}: ConstructIdentifier) {
        this.setState({
            maxInvocationDepth: -1,
            selectedConstruct: {
                constructName, moduleName, subConstructName
            },
        });
        this.handleReset();
    }

    public componentDidMount() {
        this.updateAST();
        if (this.props.initialSelectedConstruct) {
            this.setState({
                selectedConstruct: this.props.initialSelectedConstruct,
            });
        }
    }

    public render() {
        const { modules } = this.state;

        const {
            selectedASTs,
            selectedUri,
        } = this.getSelected(this.state.selectedConstruct);

        if (selectedASTs) {
            // Initialize AST node view state
            selectedASTs.forEach((ast) => {
                ASTUtil.traversNode(ast, initVisitor);
            });
            setProjectAST(modules);
            setMaxInvocationDepth(this.state.maxInvocationDepth === undefined ? -1 : this.state.maxInvocationDepth);
            selectedASTs.forEach((ast) => {
                ASTUtil.traversNode(ast, invocationVisitor);
            });
        }

        return (
            <div style={{height: "100%"}}>
                <TopMenu
                    modes={modes}
                    handleModeChange={this.handleModeChange}
                    selectedModeText={this.state.modeText}
                    openedState={this.state.openedState}
                    handleBackClick={this.handleBackClick}
                    handleFitClick={this.handleFitClick}
                    handleZoomIn={this.handleZoomIn}
                    handleZoomOut={this.handleZoomOut}
                    handleOpened={this.handleOpened}
                    handleClosed={this.handleClosed}
                    zoomFactor={this.state.zoomFactor}
                    handleReset={this.handleReset}
                    handleDepthSelect={this.setMaxInvocationDepth}
                    maxInvocationDepth={this.state.maxInvocationDepth}
                    reachedInvocationDepth={getReachedInvocationDepth()}
                />
                <Diagram astList={selectedASTs}
                    langClient={this.props.langClient}
                    projectAst={modules}
                    docUri={selectedUri}
                    zoom={0} height={0} width={1000}
                    mode={this.state.mode}
                    setPanZoomComp={this.setPanZoomComp}
                    maxInvocationDepth={this.state.maxInvocationDepth}>
                </Diagram>
            </div>
        );
    }

    private getSelected(selectedConstructDetails?: ConstructIdentifier): {
        selectedASTs: ASTNode[] | undefined,
        selectedUri: string,
    } {
        if (!selectedConstructDetails) {
            return {selectedASTs: this.getConstructsInFile(this.props.docUri), selectedUri: this.props.docUri};
        }

        const selectedModule = selectedConstructDetails.moduleName;
        const selectedConstruct = selectedConstructDetails.constructName;
        const selectedSubConstruct = selectedConstructDetails.subConstructName;
        const moduleList = this.getModuleList();

        let selectedAST;
        let selectedUri = "";

        moduleList.forEach((module) => {
            if (selectedModule === module.name) {
                module.nodeInfo.forEach((nodeI) => {
                    const nodeName = (nodeI.node as any).name.value;

                    if (selectedConstruct && (nodeName === selectedConstruct)) {
                        selectedAST = nodeI.node;
                        selectedUri = nodeI.uri;

                        if (selectedSubConstruct) {
                            if (ASTKindChecker.isService(selectedAST)) {
                                selectedAST = selectedAST.resources.find((resorce) => {
                                    return resorce.name.value === selectedSubConstruct;
                                });
                            }
                        }
                    }
                });
            }
        });

        return {
            selectedASTs: selectedAST ? [selectedAST] : undefined,
            selectedUri
        };
    }

    private getModuleList(): Array<{name: string, nodeInfo: Array<{node: ASTNode, uri: string}>}> {
        const { modules } = this.state;
        const moduleList: Array<{name: string, nodeInfo: Array<{node: ASTNode, uri: string}>}>  = [];

        Object.keys(modules).map((moduleName) => {
            const module = modules[moduleName];
            const newModule: {name: string, nodeInfo: Array<{node: ASTNode, uri: string}>}
                = { name: module.name, nodeInfo: [] };

            Object.keys(module.compilationUnits).forEach((cUnitName) => {
                const cUnit = module.compilationUnits[cUnitName];

                cUnit.ast.topLevelNodes.forEach((topLevelNode) => {
                    const node = topLevelNode as ASTNode;
                    if (node.ws && DiagramUtils.isDrawable(node)) {
                        if (ASTKindChecker.isTypeDefinition(node)
                            && (node.service || !ASTUtil.isValidObjectType(node))) {
                            return;
                        }
                        newModule.nodeInfo.push({uri: cUnit.uri, node: (node as ASTNode)});
                    }
                });
            });

            moduleList.push(newModule);
        });

        return moduleList;
    }

    private getConstructsInFile(uri: string): ASTNode[] | undefined {
        const { modules } = this.state;

        if (!modules) {
            return;
        }

        let selectedFileAST: BallerinaAST | undefined;
        Object.keys(modules).forEach((moduleName) => {
            const module = modules[moduleName];
            Object.keys(module.compilationUnits).forEach((cUnitName) => {
                if (module.compilationUnits[cUnitName].uri === uri) {
                    selectedFileAST = module.compilationUnits[cUnitName].ast;
                }
            });
        });

        if (selectedFileAST) {
            return selectedFileAST.topLevelNodes.filter((node) => DiagramUtils.isDrawable(node)) as ASTNode[];
        } else {
            return;
        }
    }

    private handleConstructClick(e: React.MouseEvent<HTMLAnchorElement, MouseEvent>, props: ListItemProps) {
        this.setState({
            selectedConstruct: {
                constructName: props.data.constructName,
                moduleName: props.data.moduleName,
            }
        });
    }

    private handleModeChange(e: React.MouseEvent<HTMLDivElement, MouseEvent>, props: DropdownItemProps) {
        this.setState({
            mode: props.data.type,
            modeText: props.data.text,
        });
    }

    private handleBackClick() {
        this.setState({
            selectedConstruct: undefined,
        });
    }

    private handleFitClick() {
        if (!(this.panZoomElement && this.panZoomElement.parentElement && this.panZoomComp)) {
            return;
        }

        const diagramWidth = this.panZoomElement.getBBox().width;
        const containerWidth = (this.panZoomElement.parentElement as unknown as SVGSVGElement).width.baseVal.value;
        const fitToWidthZoomScale = containerWidth / diagramWidth;

        this.panZoomComp.zoomAbs(0, 0, fitToWidthZoomScale);
        this.panZoomComp.moveTo(20, 20);
    }

    private handleZoomIn() {
        if (!this.panZoomComp) {
            return;
        }

        const {x, y, scale} = this.panZoomComp.getTransform();
        this.panZoomComp.zoomAbs(x, y, scale + 0.1);
        const { scale : newScale } = this.panZoomComp.getTransform();
        this.setState((state) => ({
            zoomFactor: newScale,
        }));
    }

    private handleZoomOut() {
        if (!this.panZoomComp) {
            return;
        }

        const {x, y, scale} = this.panZoomComp.getTransform();
        this.panZoomComp.zoomAbs(x, y, scale - 0.1);
        const { scale : newScale } = this.panZoomComp.getTransform();
        this.setState((state) => ({
            zoomFactor: newScale,
        }));
    }

    private handleOpened() {
        this.setState({
            openedState: true,
        });
    }

    private handleClosed() {
        this.setState({
            openedState: false,
        });
    }

    private handleReset() {
        if (this.panZoomComp && this.innitialPanZoomTransform) {
            const { x, y, scale } = this.innitialPanZoomTransform;
            this.panZoomComp.zoomAbs(0, 0, scale);
            this.panZoomComp.moveTo(x, y);
            this.setState({
                zoomFactor: scale,
            });
        }
    }

    private setMaxInvocationDepth(depth: number) {
        // reset any expandings
        if (!this.state.selectedConstruct) {
            return;
        }

        const {
            selectedASTs
        } = this.getSelected(this.state.selectedConstruct);

        if (selectedASTs) {
            selectedASTs.forEach((ast) => {
                ASTUtil.traversNode(ast, expandingResettingVisitor);
            });
        }

        this.setState({
            maxInvocationDepth: depth,
        });
    }

    private handleModuleNameSelect(e: React.MouseEvent<HTMLDivElement, MouseEvent>, props: DropdownItemProps) {
        const moduleList = this.getModuleList();
        const selectedModule = moduleList.find((module) => (module.name === props.data.name));
        if (!selectedModule) {
            return;
        }

        this.setState({
            selectedConstruct: {
                constructName: (selectedModule.nodeInfo[0].node as any).name.value,
                moduleName: props.data.name,
            }
        });
    }

    private handleConstructNameSelect(e: React.MouseEvent<HTMLDivElement, MouseEvent>, props: DropdownItemProps) {
        this.setState((state) => ({
            selectedConstruct: {
                ...state.selectedConstruct!,
                constructName: props.data.name,
            }
        }));
    }

    private setPanZoomComp(comp: PanZoom | undefined, element: SVGGElement | undefined) {
        this.panZoomComp = comp;
        this.panZoomElement = element;
        if (comp) {
            const {x, y, scale} = comp.getTransform();
            this.innitialPanZoomTransform = {x, y, scale};
            this.setState({
                zoomFactor: scale,
            });

            comp.on("zoom", (e: PanZoom) => {
                const { scale: newScale } = e.getTransform();
                this.setState({
                    zoomFactor: newScale,
                });
            });
        }
    }
}
