myGraphEditor = function (chromeless, themes, model, graph, editable) {
    mxEventSource.call(this);
    this.chromeless = (chromeless != null) ? chromeless : this.chromeless;
    this.graph = graph || this.createGraph(themes, model);
    this.undoManager = this.createUndoManager();
}
/**
 * Editor inherits from mxEventSource
 */
mxUtils.extend(myGraphEditor, mxEventSource);



/**
 * Specifies if the canvas should be extended in all directions. Default is true.
 */
myGraphEditor.prototype.extendCanvas = true;

myGraphEditor.prototype.initialTopSpacing = 0;

/**
 * Specifies the image URL to be used for the transparent background.
 */
myGraphEditor.ctrlKey = (mxClient.IS_MAC) ? 'Cmd' : 'Ctrl';
/**
 * Specifies the image URL to be used for the transparent background.
 */
myGraphEditor.hintOffset = 20;


myGraphEditor.lightCheckmarkImage =  'data:image/gif;base64,R0lGODlhFQAVAMQfAGxsbHx8fIqKioaGhvb29nJycvr6+sDAwJqamltbW5OTk+np6YGBgeTk5Ly8vJiYmP39/fLy8qWlpa6ursjIyOLi4vj4+N/f3+3t7fT09LCwsHZ2dubm5r6+vmZmZv///yH/C1hNUCBEYXRhWE1QPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS4wLWMwNjAgNjEuMTM0Nzc3LCAyMDEwLzAyLzEyLTE3OjMyOjAwICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOnhtcE1NPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvbW0vIiB4bWxuczpzdFJlZj0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL3NUeXBlL1Jlc291cmNlUmVmIyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ1M1IFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6OEY4NTZERTQ5QUFBMTFFMUE5MTVDOTM5MUZGMTE3M0QiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6OEY4NTZERTU5QUFBMTFFMUE5MTVDOTM5MUZGMTE3M0QiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo4Rjg1NkRFMjlBQUExMUUxQTkxNUM5MzkxRkYxMTczRCIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo4Rjg1NkRFMzlBQUExMUUxQTkxNUM5MzkxRkYxMTczRCIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PgH//v38+/r5+Pf29fTz8vHw7+7t7Ovq6ejn5uXk4+Lh4N/e3dzb2tnY19bV1NPS0dDPzs3My8rJyMfGxcTDwsHAv769vLu6ubi3trW0s7KxsK+urayrqqmop6alpKOioaCfnp2cm5qZmJeWlZSTkpGQj46NjIuKiYiHhoWEg4KBgH9+fXx7enl4d3Z1dHNycXBvbm1sa2ppaGdmZWRjYmFgX15dXFtaWVhXVlVUU1JRUE9OTUxLSklIR0ZFRENCQUA/Pj08Ozo5ODc2NTQzMjEwLy4tLCsqKSgnJiUkIyIhIB8eHRwbGhkYFxYVFBMSERAPDg0MCwoJCAcGBQQDAgEAACH5BAEAAB8ALAAAAAAVABUAAAVI4CeOZGmeaKqubKtylktSgCOLRyLd3+QJEJnh4VHcMoOfYQXQLBcBD4PA6ngGlIInEHEhPOANRkaIFhq8SuHCE1Hb8Lh8LgsBADs=' ;
myGraphEditor.checkmarkImage = myGraphEditor.lightCheckmarkImage;
/**
 * Default length for global unique IDs.
 */
myGraphEditor.GUID_LENGTH = 20;
/**
 * Alphabet for global unique IDs.
 */
myGraphEditor.GUID_ALPHABET = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_';

/**
 * Default length for global unique IDs.
 */
myGraphEditor.guid = function(length)
{
    var len = (length != null) ? length : myGraphEditor.GUID_LENGTH;
    var rtn = [];

    for (var i = 0; i < len; i++)
    {
        rtn.push(myGraphEditor.GUID_ALPHABET.charAt(Math.floor(Math.random() * myGraphEditor.GUID_ALPHABET.length)));
    }

    return rtn.join('');
};
myGraphEditor.mathJaxQueue = [];
// Adds global clear queue method
myGraphEditor.MathJaxClear = function()
{
    myGraphEditor.mathJaxQueue = [];
};

/**
 * Keeps the graph container in sync with the persistent graph state
 */
myGraphEditor.prototype.updateGraphComponents = function () {
    var graph = this.graph;

    if (graph.container != null) {
        graph.view.validateBackground();
        graph.container.style.overflow = (graph.scrollbars) ? 'auto' : this.defaultGraphOverflow;

        this.fireEvent(new mxEventObject('updateGraphComponents'));
    }
};
myGraphEditor.prototype.groupCells = function (vlanName) {
    var border = (this.groupBorderSize != null) ?
        this.groupBorderSize :
        this.graph.gridSize;
    var mode=this.createGroup();
    mode.value=vlanName;
    return this.graph.groupCells(mode, border);
};

/**
 * Function: createGroup
 *
 * Creates and returns a clone of <defaultGroup> to be used
 * as a new group cell in <group>.
 */
myGraphEditor.prototype.createGroup = function () {
    var model = this.graph.getModel();

    return model.cloneCell(this.defaultGroup);
};

/**
 * Sets the XML node for the current diagram.
 */
myGraphEditor.prototype.createGraph = function (themes, model) {
    var graph = new Graph(null, model, null, null, themes);
    graph.transparentBackground = false;

    return graph;
};


/**
 * Creates and returns a new undo manager.
 */
myGraphEditor.prototype.createUndoManager = function () {
    var graph = this.graph;
    var undoMgr = new mxUndoManager();

    this.undoListener = function (sender, evt) {
        undoMgr.undoableEditHappened(evt.getProperty('edit'));
    };

    // Installs the command history
    var listener = mxUtils.bind(this, function (sender, evt) {
        this.undoListener.apply(this, arguments);
    });

    graph.getModel().addListener(mxEvent.UNDO, listener);
    graph.getView().addListener(mxEvent.UNDO, listener);

    // Keeps the selection in sync with the history
    var undoHandler = function (sender, evt) {
        var cand = graph.getSelectionCellsForChanges(evt.getProperty('edit').changes);
        var model = graph.getModel();
        var cells = [];

        for (var i = 0; i < cand.length; i++) {
            if (graph.view.getState(cand[i]) != null) {
                cells.push(cand[i]);
            }
        }

        graph.setSelectionCells(cells);
    };

    undoMgr.addListener(mxEvent.UNDO, undoHandler);
    undoMgr.addListener(mxEvent.REDO, undoHandler);

    return undoMgr;
};


/**
 * Helper function to extract the graph model XML node.
 */
myGraphEditor.prototype.extractGraphModel = function(node, allowMxFile, checked)
{
    return myGraphEditor.extractGraphModel.apply(this, arguments);
};


/**
 * Helper function to extract the graph model XML node.
 */
myGraphEditor.extractGraphModel = function(node, allowMxFile, checked)
{
    if (node != null && typeof(pako) !== 'undefined')
    {
        var tmp = node.ownerDocument.getElementsByTagName('div');
        var divs = [];

        if (tmp != null && tmp.length > 0)
        {
            for (var i = 0; i < tmp.length; i++)
            {
                if (tmp[i].getAttribute('class') == 'mxgraph')
                {
                    divs.push(tmp[i]);
                    break;
                }
            }
        }

        if (divs.length > 0)
        {
            var data = divs[0].getAttribute('data-mxgraph');

            if (data != null)
            {
                var config = JSON.parse(data);

                if (config != null && config.xml != null)
                {
                    var doc2 = mxUtils.parseXml(config.xml);
                    node = doc2.documentElement;
                }
            }
            else
            {
                var divs2 = divs[0].getElementsByTagName('div');

                if (divs2.length > 0)
                {
                    var data = mxUtils.getTextContent(divs2[0]);
                    data = Graph.decompress(data, null, checked);

                    if (data.length > 0)
                    {
                        var doc2 = mxUtils.parseXml(data);
                        node = doc2.documentElement;
                    }
                }
            }
        }
    }

    if (node != null && node.nodeName == 'svg')
    {
        var tmp = node.getAttribute('content');

        if (tmp != null && tmp.charAt(0) != '<' && tmp.charAt(0) != '%')
        {
            tmp = unescape((window.atob) ? atob(tmp) : Base64.decode(cont, tmp));
        }

        if (tmp != null && tmp.charAt(0) == '%')
        {
            tmp = decodeURIComponent(tmp);
        }

        if (tmp != null && tmp.length > 0)
        {
            node = mxUtils.parseXml(tmp).documentElement;
        }
        else
        {
            throw {message: mxResources.get('notADiagramFile')};
        }
    }

    if (node != null && !allowMxFile)
    {
        var diagramNode = null;

        if (node.nodeName == 'diagram')
        {
            diagramNode = node;
        }
        else if (node.nodeName == 'mxfile')
        {
            var diagrams = node.getElementsByTagName('diagram');

            if (diagrams.length > 0)
            {
                diagramNode = diagrams[Math.max(0, Math.min(diagrams.length - 1, urlParams['page'] || 0))];
            }
        }

        if (diagramNode != null)
        {
            node = myGraphEditor.parseDiagramNode(diagramNode, checked);
        }
    }

    if (node != null && node.nodeName != 'mxGraphModel' && (!allowMxFile || node.nodeName != 'mxfile'))
    {
        node = null;
    }

    return node;
};
/**
 * Extracts the XML from the compressed or non-compressed text chunk.
 */
myGraphEditor.parseDiagramNode = function(diagramNode, checked)
{
    var text = mxUtils.trim(mxUtils.getTextContent(diagramNode));
    var node = null;

    if (text.length > 0)
    {
        var tmp = Graph.decompress(text, null, checked);

        if (tmp != null && tmp.length > 0)
        {
            node = mxUtils.parseXml(tmp).documentElement;
        }
    }
    else
    {
        var temp = mxUtils.getChildNodes(diagramNode);

        if (temp.length > 0)
        {
            // Creates new document for unique IDs within mxGraphModel
            var doc = mxUtils.createXmlDocument();
            doc.appendChild(doc.importNode(temp[0], true));
            node = doc.documentElement;
        }
    }

    return node;
};

/**
 * Sets the XML node for the current diagram.
 */
myGraphEditor.prototype.readGraphState = function(node)
{
    var grid = node.getAttribute('grid');

    if (grid == null || grid == '')
    {
        grid = this.graph.defaultGridEnabled ? '1' : '0';
    }

    this.graph.gridEnabled = false;
    this.graph.gridSize = parseFloat(node.getAttribute('gridSize')) || mxGraph.prototype.gridSize;
    this.graph.graphHandler.guidesEnabled = node.getAttribute('guides') != '0';
    this.graph.setTooltips(node.getAttribute('tooltips') != '0');
    this.graph.setConnectable(node.getAttribute('connect') != '0');
    this.graph.connectionArrowsEnabled = node.getAttribute('arrows') != '0';
    this.graph.foldingEnabled = node.getAttribute('fold') != '0';

    if ( this.graph.foldingEnabled)
    {
        this.graph.foldingEnabled = false;
        this.graph.cellRenderer.forceControlClickHandler = this.graph.foldingEnabled;
    }

    var ps = parseFloat(node.getAttribute('pageScale'));

    if (!isNaN(ps) && ps > 0)
    {
        this.graph.pageScale = ps;
    }
    else
    {
        this.graph.pageScale = mxGraph.prototype.pageScale;
    }

    if (!this.graph.isLightboxView() && !this.graph.isViewer())
    {
        var pv = node.getAttribute('page');

        if (pv != null)
        {
            this.graph.pageVisible = (pv != '0');
        }
        else
        {
            this.graph.pageVisible = this.graph.defaultPageVisible;
        }
    }
    else
    {
        this.graph.pageVisible = false;
    }

    this.graph.pageBreaksVisible = this.graph.pageVisible;
    this.graph.preferPageSize = this.graph.pageBreaksVisible;

    var pw = parseFloat(node.getAttribute('pageWidth'));
    var ph = parseFloat(node.getAttribute('pageHeight'));

    if (!isNaN(pw) && !isNaN(ph))
    {
        this.graph.pageFormat = new mxRectangle(0, 0, pw, ph);
    }

    // Loads the persistent state settings
    var bg = node.getAttribute('background');

    if (bg != null && bg.length > 0)
    {
        this.graph.background = bg;
    }
    else
    {
        this.graph.background = null;
    }
};

/**
 * Sets the XML node for the current diagram.
 */
myGraphEditor.prototype.setGraphXmlEditor = function(node)
{
    if (node != null)
    {
        var dec = new mxCodec(node.ownerDocument);

        if (node.nodeName == 'mxGraphModel')
        {
            this.graph.model.beginUpdate();

            try
            {
                this.graph.model.clear();
                this.graph.view.scale = 1;
                this.readGraphState(node);
                this.updateGraphComponents();
                dec.decode(node, this.graph.getModel());
            }
            finally
            {
                this.graph.model.endUpdate();
            }

            this.fireEvent(new mxEventObject('resetGraphView'));
        }
        else if (node.nodeName == 'root')
        {
            this.resetGraph();

            // Workaround for invalid XML output in Firefox 20 due to bug in mxUtils.getXml
            var wrapper = dec.document.createElement('mxGraphModel');
            wrapper.appendChild(node);

            dec.decode(wrapper, this.graph.getModel());
            this.updateGraphComponents();
            this.fireEvent(new mxEventObject('resetGraphView'));
        }
        else
        {
            throw {
                message: mxResources.get('cannotOpenFile'),
                node: node,
                toString: function() { return this.message; }
            };
        }
    }
    else
    {
        this.resetGraph();
        this.graph.model.clear();
        this.fireEvent(new mxEventObject('resetGraphView'));
    }
};

var editorSetGraphXml = myGraphEditor.prototype.setGraphXmlEditor;


/**
 * Returns the XML node that represents the current diagram.
 */
myGraphEditor.prototype.getGraphXmlEditor = function(ignoreSelection)
{
    ignoreSelection = (ignoreSelection != null) ? ignoreSelection : true;
    var node = null;

    if (ignoreSelection)
    {
        var enc = new mxCodec(mxUtils.createXmlDocument());
        node = enc.encode(this.graph.getModel());
    }
    else
    {
        node = this.graph.encodeCells(mxUtils.sortCells(this.graph.model.getTopmostCells(
            this.graph.getSelectionCells())));
    }

    if (this.graph.view.translate.x != 0 || this.graph.view.translate.y != 0)
    {
        node.setAttribute('dx', Math.round(this.graph.view.translate.x * 100) / 100);
        node.setAttribute('dy', Math.round(this.graph.view.translate.y * 100) / 100);
    }

    node.setAttribute('grid', (this.graph.isGridEnabled()) ? '1' : '0');
    node.setAttribute('gridSize', this.graph.gridSize);
    node.setAttribute('guides', (this.graph.graphHandler.guidesEnabled) ? '1' : '0');
    node.setAttribute('tooltips', (this.graph.tooltipHandler.isEnabled()) ? '1' : '0');
    node.setAttribute('connect', (this.graph.connectionHandler.isEnabled()) ? '1' : '0');
    node.setAttribute('arrows', (this.graph.connectionArrowsEnabled) ? '1' : '0');
    node.setAttribute('fold', (this.graph.foldingEnabled) ? '1' : '0');
    node.setAttribute('page', (this.graph.pageVisible) ? '1' : '0');
    node.setAttribute('pageScale', this.graph.pageScale);
    node.setAttribute('pageWidth', this.graph.pageFormat.width);
    node.setAttribute('pageHeight', this.graph.pageFormat.height);

    if (this.graph.background != null)
    {
        node.setAttribute('background', this.graph.background);
    }

    return node;
};


/**
 * Adds persistent style to file
 */
var editorGetGraphXml = myGraphEditor.prototype.getGraphXmlEditor;


myGraphEditor.prototype.execute = function (actionname, cell, evt) {
    var action = this.actions[actionname];
    if (action != null) {
        try {
            // Creates the array of arguments by replacing the actionname
            // with the editor instance in the args of this function
            var args = arguments;
            args[0] = this;
            // Invokes the function on the editor using the args
            action.funct.apply(this, args);
        } catch (e) {
            mxUtils.error('Cannot execute ' + actionname +
                ': ' + e.message, 280, true);

            throw e;
        }
    } else {
        mxUtils.error('Cannot find action ' + actionname, 280, true);
    }
};


/**
 * Extracts any parsers errors in the given XML.
 */
myGraphEditor.extractParserError = function(node, defaultCause)
{
    var cause = null;
    var errors = (node != null) ? node.getElementsByTagName('parsererror') : null;

    if (errors != null && errors.length > 0)
    {
        cause = defaultCause || mxResources.get('invalidChars');
        var divs = errors[0].getElementsByTagName('div');

        if (divs.length > 0)
        {
            cause = mxUtils.getTextContent(divs[0]);
        }
    }

    return (cause != null) ? mxUtils.trim(cause) : cause;
};

/**
 * Sets the XML node for the current diagram.
 */
myGraphEditor.prototype.resetGraph = function () {
    this.graph.gridEnabled = true;
    this.graph.graphHandler.guidesEnabled = true;
    this.graph.setTooltips(true);
    this.graph.setConnectable(true);
    this.graph.foldingEnabled = true;
    this.graph.scrollbars = this.graph.defaultScrollbars;
    this.graph.pageVisible = this.graph.defaultPageVisible;
    this.graph.pageBreaksVisible = this.graph.pageVisible;
    this.graph.preferPageSize = this.graph.pageBreaksVisible;
    this.graph.background = null;
    this.graph.pageScale = mxGraph.prototype.pageScale;
    this.graph.pageFormat = mxGraph.prototype.pageFormat;
    this.graph.currentScale = 1;
    this.graph.currentTranslate.x = 0;
    this.graph.currentTranslate.y = 0;
    this.updateGraphComponents();
    this.graph.view.setScale(1);
};

(function () {

    // Uses HTML for background pages (to support grid background image)
    mxGraphView.prototype.validateBackgroundPage = function () {
        var graph = this.graph;
        if (graph.container != null && !graph.transparentBackground) {
            if (graph.pageVisible) {
                var bounds = this.getBackgroundPageBounds();

                if (this.backgroundPageShape == null) {
                    // Finds first element in graph container
                    var firstChild = graph.container.firstChild;

                    while (firstChild != null && firstChild.nodeType != mxConstants.NODETYPE_ELEMENT) {
                        firstChild = firstChild.nextSibling;
                    }

                    if (firstChild != null) {
                        this.backgroundPageShape = this.createBackgroundPageShape(bounds);
                        this.backgroundPageShape.scale = 1;
                        // Shadow filter causes problems in outline window in quirks mode. IE8 standards
                        // also has known rendering issues inside mxWindow but not using shadow is worse.
                        this.backgroundPageShape.isShadow = !mxClient.IS_QUIRKS;
                        this.backgroundPageShape.dialect = mxConstants.DIALECT_STRICTHTML;
                        this.backgroundPageShape.init(graph.container);
                        // Required for the browser to render the background page in correct order
                        firstChild.style.position = 'absolute';
                        graph.container.insertBefore(this.backgroundPageShape.node, firstChild);
                        this.backgroundPageShape.redraw();
                        this.backgroundPageShape.node.className = 'geBackgroundPage';
                    }
                } else {
                    this.backgroundPageShape.scale = 1;
                    this.backgroundPageShape.bounds = bounds;
                    this.backgroundPageShape.redraw();
                }
            } else if (this.backgroundPageShape != null) {
                this.backgroundPageShape.destroy();
                this.backgroundPageShape = null;
            }

            this.validateBackgroundStyles();
        }
    };

    // Updates the CSS of the background to draw the grid
    mxGraphView.prototype.validateBackgroundStyles = function () {
        var graph = this.graph;
        var color = (graph.background == null || graph.background == mxConstants.NONE) ? graph.defaultPageBackgroundColor : graph.background;
        var gridColor = (color != null && this.gridColor != color.toLowerCase()) ? this.gridColor : '#ffffff';
        var image = 'none';
        var position = '';

        if (graph.isGridEnabled()) {
            var phase = 10;

            if (mxClient.IS_SVG) {
                // Generates the SVG required for drawing the dynamic grid
                //画成方格式的样子,动态画格子
                image = unescape(encodeURIComponent(this.createSvgGrid(gridColor)));
                image = (window.btoa) ? btoa(image) : Base64.encode(image, true);
                image = 'url(' + 'data:image/svg+xml;base64,' + image + ')'
                phase = graph.gridSize * this.scale * this.gridSteps;
            } else {
                // Fallback to grid wallpaper with fixed size
                //变为默认的样子
                image = 'url(' + this.gridImage + ')';
            }
            var x0 = 0;
            var y0 = 0;
            if (graph.view.backgroundPageShape != null) {
                var bds = this.getBackgroundPageBounds();
                x0 = 1 + bds.x;
                y0 = 1 + bds.y;
            }

            // Computes the offset to maintain origin for grid
            position = -Math.round(phase - mxUtils.mod(this.translate.x * this.scale - x0, phase)) + 'px ' +
                -Math.round(phase - mxUtils.mod(this.translate.y * this.scale - y0, phase)) + 'px';
        }
        if (graph.view.backgroundPageShape != null) {
            graph.view.backgroundPageShape.node.style.backgroundPosition = position;
            graph.view.backgroundPageShape.node.style.backgroundImage = image;
            graph.view.backgroundPageShape.node.style.backgroundColor = color;
            graph.container.className = 'geDiagramContainer geDiagramBackdrop';
        } else {
            graph.container.className = 'geDiagramContainer';
        }
    };

    // Returns the SVG required for painting the background grid.
    mxGraphView.prototype.createSvgGrid = function (color) {
        var tmp = this.graph.gridSize * this.scale;
        while (tmp < this.minGridSize) {
            tmp *= 2;
        }
        var tmp2 = this.gridSteps * tmp;
        // Small grid lines
        var d = [];
        for (var i = 1; i < this.gridSteps; i++) {
            var tmp3 = i * tmp;
            d.push('M 0 ' + tmp3 + ' L ' + tmp2 + ' ' + tmp3 + ' M ' + tmp3 + ' 0 L ' + tmp3 + ' ' + tmp2);
        }
        // KNOWN: Rounding errors for certain scales (eg. 144%, 121% in Chrome, FF and Safari). Workaround
        // in Chrome is to use 100% for the svg size, but this results in blurred grid for large diagrams.
        var size = tmp2;
        var svg = '<svg width="' + size + '" height="' + size + '" xmlns="' + mxConstants.NS_SVG + '">' +
            '<defs><pattern id="grid" width="' + tmp2 + '" height="' + tmp2 + '" patternUnits="userSpaceOnUse">' +
            '<path d="' + d.join(' ') + '" fill="none" stroke="' + color + '" opacity="0.2" stroke-width="1"/>' +
            '<path d="M ' + tmp2 + ' 0 L 0 0 0 ' + tmp2 + '" fill="none" stroke="' + color + '" stroke-width="1"/>' +
            '</pattern></defs><rect width="100%" height="100%" fill="url(#grid)"/></svg>';

        return svg;
    };


    // Draws page breaks only within the page
    //如果节点都在page的范围之内去掉breaks（类似于边框）
    mxGraph.prototype.updatePageBreaks = function (visible, width, height) {
        var scale = this.view.scale;
        var tr = this.view.translate;
        var fmt = this.pageFormat;
        var ps = scale * this.pageScale;

        var bounds2 = this.view.getBackgroundPageBounds();

        width = bounds2.width;
        height = bounds2.height;
        var bounds = new mxRectangle(scale * tr.x, scale * tr.y, fmt.width * ps, fmt.height * ps);

        // Does not show page breaks if the scale is too small
        visible = visible && Math.min(bounds.width, bounds.height) > this.minPageBreakDist;

        var horizontalCount = (visible) ? Math.ceil(height / bounds.height) - 1 : 0;
        var verticalCount = (visible) ? Math.ceil(width / bounds.width) - 1 : 0;
        var right = bounds2.x + width;
        var bottom = bounds2.y + height;

        if (this.horizontalPageBreaks == null && horizontalCount > 0) {
            this.horizontalPageBreaks = [];
        }

        if (this.verticalPageBreaks == null && verticalCount > 0) {
            this.verticalPageBreaks = [];
        }

        var drawPageBreaks = mxUtils.bind(this, function (breaks) {
            if (breaks != null) {
                var count = (breaks == this.horizontalPageBreaks) ? horizontalCount : verticalCount;

                for (var i = 0; i <= count; i++) {
                    var pts = (breaks == this.horizontalPageBreaks) ? [new mxPoint(Math.round(bounds2.x), Math.round(bounds2.y + (i + 1) * bounds.height)),
                        new mxPoint(Math.round(right), Math.round(bounds2.y + (i + 1) * bounds.height))
                    ] : [new mxPoint(Math.round(bounds2.x + (i + 1) * bounds.width), Math.round(bounds2.y)),
                        new mxPoint(Math.round(bounds2.x + (i + 1) * bounds.width), Math.round(bottom))
                    ];

                    if (breaks[i] != null) {
                        breaks[i].points = pts;
                        breaks[i].redraw();
                    } else {
                        var pageBreak = new mxPolyline(pts, this.pageBreakColor);
                        pageBreak.dialect = this.dialect;
                        pageBreak.isDashed = this.pageBreakDashed;
                        pageBreak.pointerEvents = false;
                        pageBreak.init(this.view.backgroundPane);
                        pageBreak.redraw();

                        breaks[i] = pageBreak;
                    }
                }

                for (var i = count; i < breaks.length; i++) {
                    breaks[i].destroy();
                }

                breaks.splice(count, breaks.length - count);
            }
        });

        drawPageBreaks(this.horizontalPageBreaks);
        drawPageBreaks(this.verticalPageBreaks);
    };


    // Creates background page shape
    mxGraphView.prototype.createBackgroundPageShape = function (bounds) {
        return new mxRectangleShape(bounds, '#ffffff', this.graph.defaultPageBorderColor);
    };






    // Fits the number of background pages to the graph
    mxGraphView.prototype.getBackgroundPageBounds = function () {
        var gb = this.getGraphBounds();

        // Computes unscaled, untranslated graph bounds
        var x = (gb.width > 0) ? gb.x / this.scale - this.translate.x : 0;
        var y = (gb.height > 0) ? gb.y / this.scale - this.translate.y : 0;
        var w = gb.width / this.scale;
        var h = gb.height / this.scale;

        var fmt = this.graph.pageFormat;
        var ps = this.graph.pageScale;

        var pw = fmt.width * ps;
        var ph = fmt.height * ps;

        var x0 = Math.floor(Math.min(0, x) / pw);
        var y0 = Math.floor(Math.min(0, y) / ph);
        var xe = Math.ceil(Math.max(1, x + w) / pw);
        var ye = Math.ceil(Math.max(1, y + h) / ph);

        var rows = xe - x0;
        var cols = ye - y0;

        var bounds = new mxRectangle(this.scale * (this.translate.x + x0 * pw), this.scale *
            (this.translate.y + y0 * ph), this.scale * rows * pw, this.scale * cols * ph);
        return bounds;
    };




})();