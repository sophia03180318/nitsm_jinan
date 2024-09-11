
/**
 * Constructs a new graph instance. Note that the constructor does not take a
 * container because the graph instance is needed for creating the UI, which
 * in turn will create the container for the graph. Hence, the container is
 * assigned later in EditorUi.
 */
/**
 * Defines graph class.
 */
Graph = function (container, model, renderHint, stylesheet, themes, standalone) {
    mxGraph.call(this, container, model, renderHint, stylesheet);
    // 顶点样式
    var style = new Object();
    style[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_IMAGE;
    style[mxConstants.STYLE_VERTICAL_LABEL_POSITION] = mxConstants.ALIGN_BOTTOM; // label在正下方
    style[mxConstants.STYLE_LABEL_BACKGROUNDCOLOR] = 'transparent';
    style[mxConstants.STYLE_LABEL_WIDTH] = 60;
    style[mxConstants.STYLE_EDITABLE]=0;//是否允许编辑，0为不可编辑
    style[mxConstants.STYLE_PERIMETER] = mxPerimeter.RectanglePerimeter;
    style[mxConstants.STYLE_ALIGN] = mxConstants.ALIGN_CENTER;
    style[mxConstants.STYLE_WHITE_SPACE] = 'wrap' //自动换行
     this.setHtmlLabels(true) //节点名称换行
    this.getStylesheet().putDefaultVertexStyle(style);//设置默认样式
    var styleEditor = new Object();
    styleEditor[mxConstants.STYLE_SHAPE] =  mxConstants.SHAPE_RECTANGLE;
    styleEditor[mxConstants.STYLE_PERIMETER] = mxPerimeter.RectanglePerimeter;
    styleEditor[mxConstants.STYLE_GRADIENTCOLOR] = '#F8C48B';
    styleEditor[mxConstants.STYLE_STROKECOLOR] = '#E86A00';
    styleEditor[mxConstants.STYLE_FONTCOLOR] = '#000000';
    styleEditor[mxConstants.STYLE_ROUNDED] = true;
    styleEditor[mxConstants.STYLE_OPACITY] = '10';
    styleEditor[mxConstants.STYLE_STARTSIZE] = '30';
    styleEditor[mxConstants.STYLE_FONTSIZE] = '16';
    styleEditor[mxConstants.STYLE_FONTSTYLE] = 1;
    styleEditor[mxConstants.STYLE_VERTICAL_LABEL_POSITION] = mxConstants.ALIGN_CENTER; // label正中间
    styleEditor[mxConstants.STYLE_ALIGN] = mxConstants.ALIGN_CENTER;
    styleEditor[mxConstants.STYLE_WHITE_SPACE] = 'nowrap' //不换行
    styleEditor[mxConstants.STYLE_EDITABLE]=1;//是否允许编辑，0为不可编辑

    this.getStylesheet().putCellStyle('editorMark', styleEditor);//【编辑备注】的样式





    this.themes = themes || this.defaultThemes;
    this.currentEdgeStyle = mxUtils.clone(this.defaultEdgeStyle);
    this.standalone = (standalone != null) ? standalone : false;
    // All code below not available and not needed in embed mode
    if (typeof mxVertexHandler !== 'undefined') {
        this.setConnectable(true);
        this.setPanning(true);
        this.allowAutoPanning = true;
        this.resetEdgesOnConnect = false;

        // Do not scroll after moving cells
        this.graphHandler.scrollOnMove = false;
        this.graphHandler.scaleGrid = true;

        // Disables cloning of connection sources by default
        this.connectionHandler.setCreateTarget(false);
        this.connectionHandler.insertBeforeSource = true;

        // Disables built-in connection starts
        this.connectionHandler.isValidSource = function (cell, me) {
            return false;
        };

        // Removes folding icon for relative children
        this.isCellFoldable = function (cell, collapse) {
            var childCount = this.model.getChildCount(cell);

            for (var i = 0; i < childCount; i++) {
                var child = this.model.getChildAt(cell, i);
                var geo = this.getCellGeometry(child);

                if (geo != null) {
                    return false;
                }
            }

            return childCount > 0;
        };

        //Switch for black background and bright styles
        var style = this.defaultEdgeStyle;
        style['edgeStyle'] = 'wireEdgeStyle';
        var style1 = this.getStylesheet().getDefaultEdgeStyle();
        delete style1['endArrow'];
        this.currentEdgeStyle = mxUtils.clone(this.defaultEdgeStyle);
        this.currentVertexStyle = mxUtils.clone(this.defaultVertexStyle);
        // Enables moving of relative children
        this.isCellLocked = function (cell) {
            return false;
        };
    }

    //Create a unique offset object for each graph instance.
    this.currentTranslate = new mxPoint(0, 0);
};

/**
 * Returns a decompressed version of the base64 encoded string.
 */
Graph.decompress = function(data, inflate, checked)
{
    if (data == null || data.length == 0 || typeof(pako) === 'undefined')
    {
        return data;
    }
    else
    {
        var tmp = Graph.stringToArrayBuffer(atob(data));
        var inflated = decodeURIComponent((inflate) ?
            pako.inflate(tmp, {to: 'string'}) :
            pako.inflateRaw(tmp, {to: 'string'}));

        return (checked) ? inflated : Graph.zapGremlins(inflated);
    }
};

/**
 * Returns an array buffer for the given string.
 */
Graph.stringToArrayBuffer = function(data)
{
    return Uint8Array.from(data, function (c)
    {
        return c.charCodeAt(0);
    });
};

/**
 * Removes all illegal control characters with ASCII code <32 except TAB, LF
 * and CR.
 */
Graph.zapGremlins = function(text)
{
    var lastIndex = 0;
    var checked = [];

    for (var i = 0; i < text.length; i++)
    {
        var code = text.charCodeAt(i);

        // Removes all control chars except TAB, LF and CR
        if (!((code >= 32 || code == 9 || code == 10 || code == 13) &&
            code != 0xFFFF && code != 0xFFFE))
        {
            checked.push(text.substring(lastIndex, i));
            lastIndex = i + 1;
        }
    }

    if (lastIndex > 0 && lastIndex < text.length)
    {
        checked.push(text.substring(lastIndex));
    }

    return (checked.length == 0) ? text : checked.join('');
};

/**
 * Helper function for creating SVG data URI.
 */
Graph.createSvgImage = function (w, h, data, coordWidth, coordHeight) {
    var tmp = unescape(encodeURIComponent(
        '<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">' +
        '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="' + w + 'px" height="' + h + 'px" ' +
        ((coordWidth != null && coordHeight != null) ? 'viewBox="0 0 ' + coordWidth + ' ' + coordHeight + '" ' : '') +
        'version="1.1">' + data + '</svg>'));

    return new mxImage('data:image/svg+xml;base64,' + ((window.btoa) ? btoa(tmp) : Base64.encode(tmp, true)), w, h)
};


/**
 * Graph inherits from mxGraph.
 */
mxUtils.extend(Graph, mxGraph);



mxGraph.prototype.isRectangle = function (cell) {
    if (cell != null) {
        if (this.model.getParent(cell) != this.model.getRoot()) {
            var state = this.view.getState(cell);
            var style = (state != null) ? state.style : this.getCellStyle(cell);

            if (style != null && !this.model.isEdge(cell)) {
                return style[mxConstants.STYLE_SHAPE] == mxConstants.SHAPE_RECTANGLE||style[mxConstants.STYLE_SHAPE] == mxConstants.SHAPE_CLOUD;
            }
        }
    }

    return false;
};

/**
 * Specifies the size of the size for "tiles" to be used for a graph with
 * scrollbars but no visible background page. A good value is large
 * enough to reduce the number of repaints that is caused for auto-
 * translation, which depends on this value, and small enough to give
 * a small empty buffer around the graph. Default is 400x400.
 */
Graph.prototype.scrollTileSize = new mxRectangle(0, 0, 400, 400);

/**
 * Allows all values in fit.
 */
Graph.prototype.connectionArrowsEnabled = true;

/**
 * Base URL for relative links.
 */
Graph.prototype.baseUrl =
    (((window != window.top) ? document.referrer :
        document.location.toString()).split('#')[0]);


/**
 * Installs child layout styles.
 */
Graph.prototype.init = function (container) {
    mxGraph.prototype.init.apply(this, arguments);

    var rubberband = new mxRubberband(this);

    this.getRubberband = function () {
        return rubberband;
    };
};

/**
 * Sets the XML node for the current diagram.
 */
Graph.prototype.isLightboxView = function () {
    return this.lightbox;
};

/**
 * Sets the XML node for the current diagram.
 */
Graph.prototype.isViewer = function () {
    return false;
};


/**
 * Returns the size of the page format scaled with the page size.
 */
Graph.prototype.getPageSize = function () {
    return (this.pageVisible) ? new mxRectangle(0, 0, this.pageFormat.width * this.pageScale,
        this.pageFormat.height * this.pageScale) : this.scrollTileSize;
};


/**
 * Returns a rectangle describing the position and count of the
 * background pages, where x and y are the position of the top,
 * left page and width and height are the vertical and horizontal
 * page count.
 */
Graph.prototype.getPageLayout = function () {
    var size = this.getPageSize();
    var bounds = this.getGraphBounds();

    if (bounds.width == 0 || bounds.height == 0) {
        return new mxRectangle(0, 0, 1, 1);
    } else {
        // Computes untransformed graph bounds
        var x = Math.ceil(bounds.x / this.view.scale - this.view.translate.x);
        var y = Math.ceil(bounds.y / this.view.scale - this.view.translate.y);
        var w = Math.floor(bounds.width / this.view.scale);
        var h = Math.floor(bounds.height / this.view.scale);

        var x0 = Math.floor(x / size.width);
        var y0 = Math.floor(y / size.height);
        var w0 = Math.ceil((x + w) / size.width) - x0;
        var h0 = Math.ceil((y + h) / size.height) - y0;

        return new mxRectangle(x0, y0, w0, h0);
    }
};


/**
 * Adds a connectable style.
 */
Graph.prototype.isCellConnectable = function (cell) {
    var state = this.view.getState(cell);
    var style = (state != null) ? state.style : this.getCellStyle(cell);

    return (style != null && style['connectable'] != null) ? style['connectable'] != '0' :
        mxGraph.prototype.isCellConnectable.apply(this, arguments);
};

Graph.prototype.getCenterInsertPoint = function(bbox)
{
    bbox = (bbox != null) ? bbox : new mxRectangle();

    if (mxUtils.hasScrollbars(this.container))
    {
        return new mxPoint(
            this.snap(Math.round((this.container.scrollLeft + this.container.clientWidth / 2) /
                this.view.scale - this.view.translate.x - bbox.width / 2)),
            this.snap(Math.round((this.container.scrollTop + this.container.clientHeight / 2) /
                this.view.scale - this.view.translate.y - bbox.height / 2)));
    }
    else
    {
        return new mxPoint(
            this.snap(Math.round(this.container.clientWidth / 2 / this.view.scale -
                this.view.translate.x - bbox.width / 2)),
            this.snap(Math.round(this.container.clientHeight / 2 / this.view.scale -
                this.view.translate.y - bbox.height / 2)));
    }
};

/**
 * Function: updateCssTransform
 *
 * Zooms out of the graph by <zoomFactor>.
 */
Graph.prototype.updateCssTransform = function()
{
    var temp = this.view.getDrawPane();

    if (temp != null)
    {
        var g = temp.parentNode;

        if (!this.useCssTransforms)
        {
            g.removeAttribute('transformOrigin');
            g.removeAttribute('transform');
        }
        else
        {
            var prev = g.getAttribute('transform');
            g.setAttribute('transformOrigin', '0 0');
            var s = Math.round(this.currentScale * 100) / 100;
            var dx = Math.round(this.currentTranslate.x * 100) / 100;
            var dy = Math.round(this.currentTranslate.y * 100) / 100;
            g.setAttribute('transform', 'scale(' + s + ',' + s + ')' +
                'translate(' + dx + ',' + dy + ')');

            // Applies workarounds only if translate has changed
            if (prev != g.getAttribute('transform'))
            {
                this.fireEvent(new mxEventObject('cssTransformChanged'),
                    'transform', g.getAttribute('transform'));
            }
        }
    }
};

/**
 * Loads the stylesheet for this graph.
 */
Graph.prototype.setShadowVisible = function(value, fireEvent)
{
    if (mxClient.IS_SVG && !mxClient.IS_SF)
    {
        fireEvent = (fireEvent != null) ? fireEvent : true;
        this.shadowVisible = value;

        if (this.shadowVisible)
        {
            this.view.getDrawPane().setAttribute('filter', 'url(#' + this.shadowId + ')');
        }
        else
        {
            this.view.getDrawPane().removeAttribute('filter');
        }

        if (fireEvent)
        {
            this.fireEvent(new mxEventObject('shadowVisibleChanged'));
        }
    }
};

/**
 * Parses the given background image.
 */
Graph.prototype.getBackgroundImageObject = function(obj)
{
    return obj;
};
/**
 * Returns a base64 encoded version of the compressed string.
 */
Graph.compress = function(data, deflate)
{
    if (data == null || data.length == 0 || typeof(pako) === 'undefined')
    {
        return data;
    }
    else
    {
        var tmp = (deflate) ? pako.deflate(encodeURIComponent(data)) :
            pako.deflateRaw(encodeURIComponent(data));

        return btoa(Graph.arrayBufferToString(new Uint8Array(tmp)));
    }
};

/**
 * Returns a decompressed version of the base64 encoded string.
 */
Graph.decompress = function(data, inflate, checked)
{
    if (data == null || data.length == 0 || typeof(pako) === 'undefined')
    {
        return data;
    }
    else
    {
        var tmp = Graph.stringToArrayBuffer(atob(data));
        var inflated = decodeURIComponent((inflate) ?
            pako.inflate(tmp, {to: 'string'}) :
            pako.inflateRaw(tmp, {to: 'string'}));

        return (checked) ? inflated : Graph.zapGremlins(inflated);
    }
};
/**
 * Returns a base64 encoded version of the compressed outer XML of the given node.
 */
Graph.compressNode = function(node, checked)
{
    var xml = mxUtils.getXml(node);

    return Graph.compress((checked) ? xml : Graph.zapGremlins(xml));
};

/**
 * Returns a string for the given array buffer.
 */
Graph.arrayBufferToString = function(buffer)
{
    var binary = '';
    var bytes = new Uint8Array(buffer);
    var len = bytes.byteLength;

    for (var i = 0; i < len; i++)
    {
        binary += String.fromCharCode(bytes[i]);
    }

    return binary;
};
/**
 * Stops all interactions and clears the selection.
 */
Graph.prototype.reset = function()
{
    if (this.isEditing())
    {
        this.stopEditing(true);
    }

    this.escape();

    if (!this.isSelectionEmpty())
    {
        this.clearSelection();
    }
};

/**
 * Selects first unlocked layer if one exists
 */
Graph.prototype.selectUnlockedLayer = function()
{
    if (this.defaultParent == null)
    {
        var childCount = this.model.getChildCount(this.model.root);
        var cell = null;
        var index = 0;

        do
        {
            cell = this.model.getChildAt(this.model.root, index);
        } while (index++ < childCount && mxUtils.getValue(this.getCellStyle(cell), 'locked', '0') == '1')

        if (cell != null)
        {
            this.setDefaultParent(cell);
        }
    }
};

/**
 * Hover icons are used for hover, vertex handler and drag from sidebar.
 */
HoverIcons = function (graph) {
    this.graph = graph;
    this.init();
};


/**
 * Up arrow.
 */
HoverIcons.prototype.arrowFill = '#29b6f2';


/**
 * These overrides are only added if mxVertexHandler is defined (ie. not in embedded graph)
 */
if (typeof mxVertexHandler != 'undefined') {
    (function () {
        // Enables fading of rubberband
        mxRubberband.prototype.fadeOut = true;

        // Overrides highlight shape for connection points
        //将连线高亮显示变成圆形
        mxConstraintHandler.prototype.createHighlightShape = function () {
            var hl = new mxEllipse(null, this.highlightColor, this.highlightColor, 0);
            hl.opacity = mxConstants.HIGHLIGHT_OPACITY;

            return hl;
        };

        // Uses current edge style for connect preview
        //创建虚线连接的时候会创建EdgeState
        mxConnectionHandler.prototype.createEdgeState = function (me) {
            var style = this.graph.createCurrentEdgeStyle();
            var edge = this.graph.createEdge(null, null, null, null, null, style);
            var state = new mxCellState(this.graph.view, edge, this.graph.getCellStyle(edge));
            for (var key in this.graph.currentEdgeStyle) {
                state.style[key] = this.graph.currentEdgeStyle[key];
            }
            if (this.sourceConstraint && this.sourceConstraint.point) {
                if (
                    (this.sourceConstraint.point.x > 0 && this.sourceConstraint.point.y == 0) ||
                    (this.sourceConstraint.point.x > 0 && this.sourceConstraint.point.y == 1)
                ) {
                    state.cell.position = false;
                } else {
                    state.cell.position = true;
                }

            }

            return state;
        };


        // Overrides live preview to keep current style
        mxConnectionHandler.prototype.updatePreview = function (valid) {
            // do not change color of preview
        };
        mxConnectionHandler.prototype.connect = function (source, target, evt, dropTarget) {
            if ((target != null || this.isCreateTarget(evt) || this.graph.allowDanglingEdges)
                &&this.constraintHandler.currentConstraint!=null&&this.sourceConstraint!=null) {
                // Uses the common parent of source and target or
                // the default parent to insert the edge
                var model = this.graph.getModel();
                var terminalInserted = false;
                var edge = null;

                model.beginUpdate();

                try {
                    if (source != null && target == null && !this.graph.isIgnoreTerminalEvent(evt) && this.isCreateTarget(evt)) {
                        target = this.createTargetVertex(evt, source);

                        if (target != null) {
                            dropTarget = this.graph.getDropTarget([target], evt, dropTarget);
                            terminalInserted = true;

                            // Disables edges as drop targets if the target cell was created
                            // FIXME: Should not shift if vertex was aligned (same in Java)
                            if (dropTarget == null || !this.graph.getModel().isEdge(dropTarget)) {
                                var pstate = this.graph.getView().getState(dropTarget);

                                if (pstate != null) {
                                    var tmp = model.getGeometry(target);
                                    tmp.x -= pstate.origin.x;
                                    tmp.y -= pstate.origin.y;
                                }
                            } else {
                                dropTarget = this.graph.getDefaultParent();
                            }

                            this.graph.addCell(target, dropTarget);
                        }
                    }

                    var parent = this.graph.getDefaultParent();

                    // if (source != null && target != null &&
                    //     model.getParent(source) == model.getParent(target) &&
                    //     model.getParent(model.getParent(source)) != model.getRoot()) {
                    //     parent = model.getParent(source);
                    //
                    //     if ((source.geometry != null && source.geometry.relative) &&
                    //         (target.geometry != null && target.geometry.relative)) {
                    //         parent = model.getParent(parent);
                    //     }
                    // }

                    // Uses the value of the preview edge state for inserting
                    // the new edge into the graph
                    var value = null;
                    var style = null;
                    if (this.edgeState != null) {
                        value = this.edgeState.cell.value;
                        style = this.edgeState.cell.style;
                    }
                    this.graph.getStylesheet();

                    var srPositonX=this.sourceConstraint.point.x;
                    var srPostionY=this.sourceConstraint.point.y;
                    if(this.sourceConstraint.position=="up"){
                        srPostionY=srPostionY-0.4;

                    }else if(this.sourceConstraint.position=="down"){
                        srPostionY=srPostionY+0.4;

                    }else if(this.sourceConstraint.position=="left"){
                        srPositonX=srPositonX-0.4;

                    }else if(this.sourceConstraint.position=="right"){
                        srPositonX=srPositonX+0.4;
                    }
                     var widthStyle=8;
                    var heightStyle=8;
                    if(style!=null&&style.indexOf("dashed=1")>-1){
                        widthStyle=5;
                        heightStyle=5;
                    }
                    var sr = this.graph.insertVertex(source, null, null, srPositonX, srPostionY, widthStyle, heightStyle, 'fontSize=9;shape=rect;resizable=0;strokeColor=#000000;fillColor=#c3d9ff', true);
                    sr.connectable = false;
                    sr.position = source.position;
                    sr.assetId = source.assetId;
                    sr.geometry.object = new mxPoint(15, 15);
                    sr.isPort = 1;

                    var tgPositonX=this.constraintHandler.currentConstraint.point.x;
                    var tgPostionY=this.constraintHandler.currentConstraint.point.y;
                    if(this.constraintHandler.currentConstraint.position=="up"){
                        tgPostionY=tgPostionY-0.4;

                    }else if(this.constraintHandler.currentConstraint.position=="down"){
                        tgPostionY=tgPostionY+0.4;

                    }else if(this.constraintHandler.currentConstraint.position=="left"){
                        tgPositonX=tgPositonX-0.4;

                    }else if(this.constraintHandler.currentConstraint.position=="right"){
                        tgPositonX=tgPositonX+0.4;
                    }

                    var tg = this.graph.insertVertex(target, null, null, tgPositonX, tgPostionY,  widthStyle, heightStyle, 'fontSize=9;shape=rect;resizable=0;strokeColor=#000000;fillColor=#c3d9ff', true);
                    tg.connectable = false;
                    tg.assetId = source.assetId;
                    tg.geometry.object = new mxPoint(15, 15);
                    tg.isPort = 1;


                    edge = this.insertEdge(parent, null, value, sr, tg, style);

                    if (edge != null) {
                        // Updates the connection constraints
                        this.graph.setConnectionConstraint(edge, sr, true, this.sourceConstraint);
                        this.graph.setConnectionConstraint(edge, tg, false, this.constraintHandler.currentConstraint);

                        // Uses geometry of the preview edge state
                        if (this.edgeState != null) {
                            model.setGeometry(edge, this.edgeState.cell.geometry);
                        }
                        var parent = this.graph.getDefaultParent();
                      //  var parent = model.getParent(source);

                        // Inserts edge before source
                        if (this.isInsertBefore(edge, source, target, evt, dropTarget)) {
                            var index = null;
                            var tmp = source;

                            while (tmp.parent != null && tmp.geometry != null &&
                            tmp.geometry.relative && tmp.parent != edge.parent) {
                                tmp = this.graph.model.getParent(tmp);
                            }

                            if (tmp != null && tmp.parent != null && tmp.parent == edge.parent) {
                                model.add(parent, edge, tmp.parent.getIndex(tmp));
                            }
                        }

                        // Makes sure the edge has a non-null, relative geometry
                        var geo = model.getGeometry(edge);

                        if (geo == null) {
                            geo = new mxGeometry();
                            geo.relative = true;

                            model.setGeometry(edge, geo);
                        }

                        // Uses scaled waypoints in geometry
                        if (this.waypoints != null && this.waypoints.length > 0) {
                            var s = this.graph.view.scale;
                            var tr = this.graph.view.translate;
                            geo.points = [];

                            for (var i = 0; i < this.waypoints.length; i++) {

                                var pt = this.waypoints[i];
                                geo.points.push(new mxPoint(pt.x / s - tr.x, pt.y / s - tr.y));
                            }
                        }


                        this.fireEvent(new mxEventObject(mxEvent.CONNECT, 'cell', edge, 'terminal', target,
                            'event', evt, 'target', dropTarget, 'terminalInserted', terminalInserted));
                    }
                } catch (e) {
                    mxLog.show();
                    mxLog.debug(e.message);
                } finally {
                    model.endUpdate();
                }

                if (this.select) {
                    this.selectCells(edge, (terminalInserted) ? target : null);
                }
            }
        };


        /**
         *
         */
        Graph.prototype.defaultVertexStyle = {};

        /**
         * Contains the default style for edges.
         */
        Graph.prototype.defaultEdgeStyle = {
            //'edgeStyle': 'orthogonalEdgeStyle',
            'rounded': '0',
            'jettySize': 'auto',
            'orthogonalLoop': '1'
        };

        /**
         * Returns the current edge style as a string.
         */
        Graph.prototype.createCurrentEdgeStyle = function () {
            var style = 'edgeStyle=' + (this.currentEdgeStyle['edgeStyle'] || 'none') + ';';

            if (this.currentEdgeStyle['shape'] != null) {
                style += 'shape=' + this.currentEdgeStyle['shape'] + ';';
            }

            if (this.currentEdgeStyle['curved'] != null) {
                style += 'curved=' + this.currentEdgeStyle['curved'] + ';';
            }

            if (this.currentEdgeStyle['rounded'] != null) {
                style += 'rounded=' + this.currentEdgeStyle['rounded'] + ';';
            }

            if (this.currentEdgeStyle['dashed'] != null) {
                style += 'dashed=' + this.currentEdgeStyle['dashed'] + ';';
            }

            if (this.currentEdgeStyle['comic'] != null) {
                style += 'comic=' + this.currentEdgeStyle['comic'] + ';';
            }

            if (this.currentEdgeStyle['jumpStyle'] != null) {
                style += 'jumpStyle=' + this.currentEdgeStyle['jumpStyle'] + ';';
            }

            if (this.currentEdgeStyle['jumpSize'] != null) {
                style += 'jumpSize=' + this.currentEdgeStyle['jumpSize'] + ';';
            }

            // Overrides the global default to match the default edge style
            if (this.currentEdgeStyle['orthogonalLoop'] != null) {
                style += 'orthogonalLoop=' + this.currentEdgeStyle['orthogonalLoop'] + ';';
            } else if (Graph.prototype.defaultEdgeStyle['orthogonalLoop'] != null) {
                style += 'orthogonalLoop=' + Graph.prototype.defaultEdgeStyle['orthogonalLoop'] + ';';
            }

            // Overrides the global default to match the default edge style
            if (this.currentEdgeStyle['jettySize'] != null) {
                style += 'jettySize=' + this.currentEdgeStyle['jettySize'] + ';';
            } else if (Graph.prototype.defaultEdgeStyle['jettySize'] != null) {
                style += 'jettySize=' + Graph.prototype.defaultEdgeStyle['jettySize'] + ';';
            }
            return style;
        };


        /**
         * Overrides method to provide connection constraints for shapes.
         */
        Graph.prototype.getAllConnectionConstraints = function (terminal, source) {
            if (terminal != null) {
                var constraints = mxUtils.getValue(terminal.style, 'points', null);

                if (constraints != null) {
                    // Requires an array of arrays with x, y (0..1), an optional
                    // [perimeter (0 or 1), dx, and dy] eg. points=[[0,0,1,-10,10],[0,1,0],[1,1]]
                    var result = [];

                    try {
                        var c = JSON.parse(constraints);

                        for (var i = 0; i < c.length; i++) {
                            var tmp = c[i];
                            result.push(new mxConnectionConstraint(new mxPoint(tmp[0], tmp[1]), (tmp.length > 2) ? tmp[2] != '0' : true,
                                null, (tmp.length > 3) ? tmp[3] : 0, (tmp.length > 4) ? tmp[4] : 0));
                        }
                    } catch (e) {
                        // ignore
                    }

                    return result;
                } else if (terminal.shape != null && terminal.shape.bounds != null) {
                    var dir = terminal.shape.direction;
                    var bounds = terminal.shape.bounds;
                    var scale = terminal.shape.scale;
                    var w = bounds.width / scale;
                    var h = bounds.height / scale;

                    if (dir == mxConstants.DIRECTION_NORTH || dir == mxConstants.DIRECTION_SOUTH) {
                        var tmp = w;
                        w = h;
                        h = tmp;
                    }

                    constraints = terminal.shape.getConstraints(terminal.style, w, h);

                    if (constraints != null) {
                        return constraints;
                    } else if (terminal.shape.stencil != null && terminal.shape.stencil.constraints != null) {
                        return terminal.shape.stencil.constraints;
                    } else if (terminal.shape.constraints != null) {
                        return terminal.shape.constraints;
                    }
                }
            }

            return null;
        };


        /**
         * Scrollbars are enabled on non-touch devices (not including Firefox because touch events
         * cannot be detected in Firefox, see above).
         */
        Graph.prototype.defaultScrollbars = true;

        /**
         * Specifies if the page should be visible for new files. Default is true.
         */
        Graph.prototype.defaultPageVisible = true;


        /**
         *
         */
        Graph.prototype.defaultPageBackgroundColor = '#ffffff';

        /**
         *
         */
        Graph.prototype.defaultPageBorderColor = '#ffffff';

        mxConstants.SHADOW_OPACITY = 0.25;
        mxConstants.SHADOWCOLOR = '#000000';
        mxConstants.VML_SHADOWCOLOR = '#d0d0d0';
        mxGraph.prototype.pageBreakColor = '#c0c0c0';
        mxGraph.prototype.pageScale = 1;

// Defines grid properties
        mxGraphView.prototype.gridImage = (mxClient.IS_SVG) ? 'data:image/gif;base64,R0lGODlhCgAKAJEAAAAAAP///8zMzP///yH5BAEAAAMALAAAAAAKAAoAAAIJ1I6py+0Po2wFADs=' :
            IMAGE_PATH + '/grid.gif';
        mxGraphView.prototype.gridSteps = 4;
        mxGraphView.prototype.minGridSize = 5;

// UrlParams is null in embed mode
        mxGraphView.prototype.defaultGridColor = '#d0d0d0';
        mxGraphView.prototype.gridColor = mxGraphView.prototype.defaultGridColor;

//Units
        mxGraphView.prototype.unit = mxConstants.POINTS;

// Hook for custom constraints
        mxShape.prototype.getConstraints = function (style, w, h) {
            return null;
        };

        /**
         * Defines the handles for the UI. Uses data-URIs to speed-up loading time where supported.
         */
        // TODO: Increase handle padding
        HoverIcons.prototype.mainHandle = (!mxClient.IS_SVG) ? new mxImage(IMAGE_PATH + '/handle-main.png', 17, 17) :
            Graph.createSvgImage(18, 18, '<circle cx="9" cy="9" r="5" stroke="#fff" fill="' + HoverIcons.prototype.arrowFill + '" stroke-width="1"/>');
        HoverIcons.prototype.secondaryHandle = (!mxClient.IS_SVG) ? new mxImage(IMAGE_PATH + '/handle-secondary.png', 17, 17) :
            Graph.createSvgImage(16, 16, '<path d="m 8 3 L 13 8 L 8 13 L 3 8 z" stroke="#fff" fill="#fca000"/>');
        HoverIcons.prototype.fixedHandle = (!mxClient.IS_SVG) ? new mxImage(IMAGE_PATH + '/handle-fixed.png', 17, 17) :
            Graph.createSvgImage(18, 18, '<circle cx="9" cy="9" r="5" stroke="#fff" fill="' + HoverIcons.prototype.arrowFill + '" stroke-width="1"/><path d="m 7 7 L 11 11 M 7 11 L 11 7" stroke="#fff"/>');
        HoverIcons.prototype.terminalHandle = (!mxClient.IS_SVG) ? new mxImage(IMAGE_PATH + '/handle-terminal.png', 17, 17) :
            Graph.createSvgImage(18, 18, '<circle cx="9" cy="9" r="5" stroke="#fff" fill="' + HoverIcons.prototype.arrowFill + '" stroke-width="1"/><circle cx="9" cy="9" r="2" stroke="#fff" fill="transparent"/>');
        HoverIcons.prototype.rotationHandle = (!mxClient.IS_SVG) ? new mxImage(IMAGE_PATH + '/handle-rotate.png', 16, 16) :
            Graph.createSvgImage(16, 16, '<path stroke="' + HoverIcons.prototype.arrowFill +
                '" fill="' + HoverIcons.prototype.arrowFill +
                '" d="M15.55 5.55L11 1v3.07C7.06 4.56 4 7.92 4 12s3.05 7.44 7 7.93v-2.02c-2.84-.48-5-2.94-5-5.91s2.16-5.43 5-5.91V10l4.55-4.45zM19.93 11c-.17-1.39-.72-2.73-1.62-3.89l-1.42 1.42c.54.75.88 1.6 1.02 2.47h2.02zM13 17.9v2.02c1.39-.17 2.74-.71 3.9-1.61l-1.44-1.44c-.75.54-1.59.89-2.46 1.03zm3.89-2.42l1.42 1.41c.9-1.16 1.45-2.5 1.62-3.89h-2.02c-.14.87-.48 1.72-1.02 2.48z"/>',
                24, 24);

        if (mxClient.IS_SVG) {
            mxConstraintHandler.prototype.pointImage = Graph.createSvgImage(5, 5, '<path d="m 0 0 L 5 5 M 0 5 L 5 0" stroke="' + HoverIcons.prototype.arrowFill + '"/>');
        }

        mxVertexHandler.prototype.handleImage = HoverIcons.prototype.mainHandle;
        mxVertexHandler.prototype.secondaryHandleImage = HoverIcons.prototype.secondaryHandle;
        mxEdgeHandler.prototype.handleImage = HoverIcons.prototype.mainHandle;
        mxEdgeHandler.prototype.terminalHandleImage = HoverIcons.prototype.terminalHandle;
        mxEdgeHandler.prototype.fixedHandleImage = HoverIcons.prototype.fixedHandle;
        mxEdgeHandler.prototype.labelHandleImage = HoverIcons.prototype.secondaryHandle;
        mxOutline.prototype.sizerImage = HoverIcons.prototype.mainHandle;


        mxEdgeHandler.prototype.straightRemoveEnabled = true;
        mxEdgeHandler.prototype.virtualBendsEnabled = true;
        mxEdgeHandler.prototype.mergeRemoveEnabled = true;


        mxGraphView.prototype.updateFloatingTerminalPoint = function (edge, start, end, source) {
        };
          //是否允许复制
        mxGraphHandler.prototype.cloneEnabled = true;
        // If connect preview is not moved away then getCellAt is used to detect the cell under
        // the mouse if the mouse is over the preview shape in IE (no event transparency), ie.
        // the built-in hit-detection of the HTML document will not be used in this case.
        mxConnectionHandler.prototype.movePreviewAway = false;
        mxConnectionHandler.prototype.waypointsEnabled = true;
        mxGraph.prototype.resetEdgesOnConnect = false;
        //子节点是否允许和父节点脱离关系
        mxGraphHandler.prototype.removeCellsFromParent = false;

        mxConnectionHandler.prototype.isStopEvent = function (me) {
            return me.getState() != null || mxEvent.isRightMouseButton(me.getEvent());
        };

    })();
}