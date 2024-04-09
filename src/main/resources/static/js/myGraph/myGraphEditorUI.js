myGraphEditorUI = function (editor, container, category, status, bottomButtons) {
    mxEventSource.call(this);
    this.editor = editor || new Editor();
    this.editor.category = category;
    this.editor.editorStatus = status;
    this.container = container || document.body;
    this.actions = new Actions(this);
    this.bottomButtons = bottomButtons;
    /**
     * Sets the initial scrollbar locations after a file was loaded.
     */
    this.editor.addListener('resetGraphView', mxUtils.bind(this, function () {
        this.resetScrollbars();
    }));
    this.init();
    // Create handler for key events
    this.keyHandler = this.createKeyHandler(editor);

    // Getter for key handler
    this.getKeyHandler = function () {
        return keyHandler;
    };
    this.open();



}
// Extends mxEventSource
mxUtils.extend(myGraphEditorUI, mxEventSource);




//创建核心展示界面
myGraphEditorUI.prototype.createDivs = function () {
    this.diagramContainer = this.createDiv('geDiagramContainer');
    this.diagramContainer.style.border = 'none';
    this.container.appendChild(this.diagramContainer);

};



/**
 * Creates the keyboard event handler for the current graph and history.
 */
myGraphEditorUI.prototype.createOutline = function (wnd) {
    var outline = new mxOutline(this.editor.graph);
    outline.border = 20;

    mxEvent.addListener(window, 'resize', function () {
        outline.update();
    });

    this.addListener('pageFormatChanged', function () {
        outline.update();
    });

    return outline;
};
/**
 * Initializes the infinite canvas.
 */
myGraphEditorUI.prototype.initCanvas = function () {
    // Initial page layout view, scrollBuffer and timer-based scrolling
    var graph = this.editor.graph;
    graph.timerAutoScroll = true;

    /**
     * Returns the padding for pages in page view with scrollbars.
     */
    graph.getPagePadding = function () {
        return new mxPoint(Math.max(0, Math.round((graph.container.offsetWidth - 34) / graph.view.scale)),
            Math.max(0, Math.round((graph.container.offsetHeight - 34) / graph.view.scale)));
    };

    // Fits the number of background pages to the graph
    graph.view.getBackgroundPageBounds = function () {
        var layout = this.graph.getPageLayout();
        var page = this.graph.getPageSize();

        return new mxRectangle(this.scale * (this.translate.x + layout.x * page.width),
            this.scale * (this.translate.y + layout.y * page.height),
            this.scale * layout.width * page.width,
            this.scale * layout.height * page.height);
    };

    graph.getPreferredPageSize = function (bounds, width, height) {
        var pages = this.getPageLayout();
        var size = this.getPageSize();

        return new mxRectangle(0, 0, pages.width * size.width, pages.height * size.height);
    };

    // Scales pages/graph to fit available size
    var resize = null;
    var ui = this;
    if (this.editor.extendCanvas) {
        /**
         * Guesses autoTranslate to avoid another repaint (see below).
         * Works if only the scale of the graph changes or if pages
         * are visible and the visible pages do not change.
         */
        var graphViewValidate = graph.view.validate;
        graph.view.validate = function () {
            if (this.graph.container != null && mxUtils.hasScrollbars(this.graph.container)) {
                var pad = this.graph.getPagePadding();
                var size = this.graph.getPageSize();

                // Updating scrollbars here causes flickering in quirks and is not needed
                // if zoom method is always used to set the current scale on the graph.
                var tx = this.translate.x;
                var ty = this.translate.y;
                this.translate.x = pad.x - (this.x0 || 0) * size.width;
                this.translate.y = pad.y - (this.y0 || 0) * size.height;
            }

            graphViewValidate.apply(this, arguments);
        };

        if (!graph.isViewer()) {
            var graphSizeDidChange = graph.sizeDidChange;
            //重新计算graph背景的位置使他居中
            graph.sizeDidChange = function () {
                if (this.container != null && mxUtils.hasScrollbars(this.container)) {
                    var pages = this.getPageLayout();
                    var pad = this.getPagePadding();
                    var size = this.getPageSize();

                    // Updates the minimum graph size
                    var minw = Math.ceil(2 * pad.x + pages.width * size.width);
                    var minh = Math.ceil(2 * pad.y + pages.height * size.height);

                    var min = graph.minimumGraphSize;

                    // LATER: Fix flicker of scrollbar size in IE quirks mode
                    // after delayed call in window.resize event handler
                    if (min == null || min.width != minw || min.height != minh) {
                        graph.minimumGraphSize = new mxRectangle(0, 0, minw, minh);
                    }

                    // Updates auto-translate to include padding and graph size
                    var dx = pad.x - pages.x * size.width;
                    var dy = pad.y - pages.y * size.height;

                    if (!this.autoTranslate && (this.view.translate.x != dx || this.view.translate.y != dy)) {
                        this.autoTranslate = true;
                        this.view.x0 = pages.x;
                        this.view.y0 = pages.y;

                        // NOTE: THIS INVOKES THIS METHOD AGAIN. UNFORTUNATELY THERE IS NO WAY AROUND THIS SINCE THE
                        // BOUNDS ARE KNOWN AFTER THE VALIDATION AND SETTING THE TRANSLATE TRIGGERS A REVALIDATION.
                        // SHOULD MOVE TRANSLATE/SCALE TO VIEW.
                        var tx = graph.view.translate.x;
                        var ty = graph.view.translate.y;
                        graph.view.setTranslate(dx, dy);

                        // LATER: Fix rounding errors for small zoom
                        graph.container.scrollLeft += Math.round((dx - tx) * graph.view.scale);
                        graph.container.scrollTop += Math.round((dy - ty) * graph.view.scale);

                        this.autoTranslate = false;

                        return;
                    }

                    graphSizeDidChange.apply(this, arguments);
                } else {
                    // Fires event but does not invoke superclass
                    this.fireEvent(new mxEventObject(mxEvent.SIZE, 'bounds', this.getGraphBounds()));
                }
            };
        }
    }
};

/**
 * Opens the current diagram via the window.opener if one exists.
 */
myGraphEditorUI.prototype.open = function () {
    // Fires as the last step if no file was loaded
    this.editor.graph.view.validate();

    // Required only in special cases where an initial file is opened
    // and the minimumGraphSize changes and CSS must be updated.
    //	this.editor.graph.sizeDidChange();
    this.editor.fireEvent(new mxEventObject('resetGraphView'));
};


//创建DIV
myGraphEditorUI.prototype.createDiv = function (classname) {
    var elt = document.createElement('div');
    elt.className = classname;

    return elt;
};

myGraphEditorUI.prototype.refresh = function (sizeDidChange) {
    sizeDidChange = (sizeDidChange != null) ? sizeDidChange : true;

    var quirks = mxClient.IS_IE && (document.documentMode == null || document.documentMode == 5);
    var w = this.container.clientWidth;
    var h = this.container.clientHeight;

    if (this.container == document.body) {
        w = document.body.clientWidth || document.documentElement.clientWidth;
        h = (quirks) ? document.body.clientHeight || document.documentElement.clientHeight : document.documentElement.clientHeight;
    }
    var off = 0;
    this.diagramContainer.style = 'right:40px; left: 500px; top: 45px; bottom: 28px; touch-action: none; overflow: auto;	'

};

/**
 * Resets the state of the scrollbars.
 * 重新设置graph.container的水平和竖直滚动条
 */
myGraphEditorUI.prototype.resetScrollbars = function () {
    var graph = this.editor.graph;
    if (mxUtils.hasScrollbars(graph.container)) {
        if (graph.pageVisible) {
            var pad = graph.getPagePadding();
            graph.container.scrollTop = Math.floor(pad.y - this.editor.initialTopSpacing) - 1;
            graph.container.scrollLeft = Math.floor(Math.min(pad.x,
                (graph.container.scrollWidth - graph.container.clientWidth) / 2)) - 1;
            // Scrolls graph to visible area
            var bounds = graph.getGraphBounds();

            if (bounds.width > 0 && bounds.height > 0) {
                if (bounds.x > graph.container.scrollLeft + graph.container.clientWidth * 0.9) {
                    graph.container.scrollLeft = Math.min(bounds.x + bounds.width - graph.container.clientWidth, bounds.x - 10);
                }

                if (bounds.y > graph.container.scrollTop + graph.container.clientHeight * 0.9) {
                    graph.container.scrollTop = Math.min(bounds.y + bounds.height - graph.container.clientHeight, bounds.y - 10);
                }
            }
        } else {

            var bounds = graph.getGraphBounds();
            var width = Math.max(bounds.width, graph.scrollTileSize.width * graph.view.scale);
            var height = Math.max(bounds.height, graph.scrollTileSize.height * graph.view.scale);
            graph.container.scrollTop = Math.floor(Math.max(0, bounds.y - Math.max(20, (graph.container.clientHeight - height) / 4)));
            graph.container.scrollLeft = Math.floor(Math.max(0, bounds.x - Math.max(0, (graph.container.clientWidth - width) / 2)));
        }
    } else {
        var b = mxRectangle.fromRectangle((graph.pageVisible) ? graph.view.getBackgroundPageBounds() : graph.getGraphBounds())
        var tr = graph.view.translate;
        var s = graph.view.scale;
        b.x = b.x / s - tr.x;
        b.y = b.y / s - tr.y;
        b.width /= s;
        b.height /= s;

        var dy = (graph.pageVisible) ? 0 : Math.max(0, (graph.container.clientHeight - b.height) / 4);

        graph.view.setTranslate(Math.floor(Math.max(0,
            (graph.container.clientWidth - b.width) / 2) - b.x + 2),
            Math.floor(dy - b.y + 1));
    }
};

// 添加工具栏按钮
myGraphEditorUI.prototype.addToolbarButton = function (editor, toolbar, action, label, image, isTransparent, title, selectFlag) {
    var button = document.createElement('button');
    button.style.fontSize = '10';
    if (image != null) {
        var img = document.createElement('img');
        img.setAttribute('src', image);
        img.style.width = '16px';
        img.style.height = '16px';
        img.style.verticalAlign = 'middle';
        img.style.marginRight = '2px';
        button.appendChild(img);
        button.style.marginRight = '12px';
        button.style.cursor = 'pointer';
        button.title = title;
    }
    if (isTransparent) {
        button.style.background = 'transparent';
        button.style.color = '#FFFFFF';
        button.style.border = 'none';
    }
    if (selectFlag != null && selectFlag == true) {
        button.style.border = '1px solid #000000';
    }

    mxEvent.addListener(button, 'click', function (evt) {
        action.funct(evt);

    });
    mxUtils.write(button, label);
    toolbar.appendChild(button);
};


/**
 * Returns the URL for a copy of this editor with no state.
 */
myGraphEditorUI.prototype.undo = function () {
    try {
        var graph = this.editor.graph;
        if (graph.isEditing()) {
            // Stops editing and executes undo on graph if native undo
            // does not affect current editing value
            var value = graph.cellEditor.textarea.innerHTML;
            document.execCommand('undo', false, null);

            if (value == graph.cellEditor.textarea.innerHTML) {
                graph.stopEditing(true);
                this.editor.undoManager.undo();
            }
        } else {
            this.editor.undoManager.undo();
        }
    } catch (e) {
        // ignore all errors
    }
};

/**
 * Returns the URL for a copy of this editor with no state.
 */
myGraphEditorUI.prototype.redo = function () {
    try {
        var graph = this.editor.graph;

        if (graph.isEditing()) {
            document.execCommand('redo', false, null);
        } else {
            this.editor.undoManager.redo();
        }
    } catch (e) {
        // ignore all errors
    }
};
/**
 * Creates the keyboard event handler for the current graph and history.
 */
myGraphEditorUI.prototype.createKeyHandler = function (editor) {
    var editorUi = this;
    var graph = this.editor.graph;
    var keyHandler = new mxKeyHandler(graph);
    var isEventIgnored = keyHandler.isEventIgnored;
    keyHandler.isEventIgnored = function (evt) {
        // Handles undo/redo/ctrl+./,/u via action and allows ctrl+b/i only if editing value is HTML (except for FF and Safari)
        return (!this.isControlDown(evt) || mxEvent.isShiftDown(evt) || (evt.keyCode != 90 && evt.keyCode != 89 &&
            evt.keyCode != 188 && evt.keyCode != 190 && evt.keyCode != 85)) && ((evt.keyCode != 66 && evt.keyCode != 73) ||
            !this.isControlDown(evt) || (this.graph.cellEditor.isContentEditing() && !mxClient.IS_FF && !mxClient.IS_SF)) &&
            isEventIgnored.apply(this, arguments);
    };

    // Ignores graph enabled state but not chromeless state
    keyHandler.isEnabledForEvent = function (evt) {
        return (!mxEvent.isConsumed(evt) && this.isGraphEvent(evt) && this.isEnabled() &&
            (editorUi.dialogs == null || editorUi.dialogs.length == 0));
    };

    // Routes command-key to control-key on Mac
    keyHandler.isControlDown = function (evt) {
        return mxEvent.isControlDown(evt) || (mxClient.IS_MAC && evt.metaKey);
    };
    // Binds keystrokes to actions
    keyHandler.bindAction = mxUtils.bind(this, function (code, control, key, shift) {
        var action = this.actions.get(key);

        if (action != null) {
            var f = function () {
                if (action.isEnabled()) {
                    action.funct();
                }
            };

            if (control) {
                if (shift) {
                    keyHandler.bindControlShiftKey(code, f);
                } else {
                    keyHandler.bindControlKey(code, f);
                }
            } else {
                if (shift) {
                    keyHandler.bindShiftKey(code, f);
                } else {
                    keyHandler.bindKey(code, f);
                }
            }
        }
    });


    var keyHandlerEscape = keyHandler.escape;
    keyHandler.escape = function (evt) {
        keyHandlerEscape.apply(this, arguments);
    };

    // Ignores enter keystroke. Remove this line if you want the
    // enter keystroke to stop editing. N, W, T are reserved.
    keyHandler.enter = function () {
    };

    if (!this.editor.chromeless || this.editor.editable) {
        keyHandler.bindAction(46, false, 'delete'); // Delete
        keyHandler.bindAction(46, true, 'deleteAll'); // Ctrl+Delete
    }	keyHandler.bindAction(90, true, 'undo'); // Ctrl+Z

    return keyHandler;
};


//---------------------------弹出框相关方法----------------------------//
/**
 * Shows the given popup menu.
 */
myGraphEditorUI.prototype.showPopupMenu = function(fn, x, y, evt)
{
    this.editor.graph.popupMenuHandler.hideMenu();
    var menu = new mxPopupMenu(fn);
    menu.div.className += ' geMenubarMenu';
    menu.smartSeparators = true;
    menu.showDisabled = true;
    menu.autoExpand = true;

    // Disables autoexpand and destroys menu when hidden
    menu.hideMenu = mxUtils.bind(this, function()
    {
        mxPopupMenu.prototype.hideMenu.apply(menu, arguments);
        menu.destroy();
    });

    menu.popup(x, y, null, evt);

    // Allows hiding by clicking on document
    this.setCurrentMenu(menu);
};

/**
 * Sets the current menu and element.
 */
myGraphEditorUI.prototype.setCurrentMenu = function(menu, elt)
{
    this.currentMenuElt = elt;
    this.currentMenu = menu;
};

/**
 * Resets the current menu and element.
 */
myGraphEditorUI.prototype.resetCurrentMenu = function()
{
    this.currentMenuElt = null;
    this.currentMenu = null;
};

/**
 * Hides and destroys the current menu.
 */
myGraphEditorUI.prototype.hideCurrentMenu = function()
{
    if (this.currentMenu != null)
    {
        this.currentMenu.hideMenu();
        this.resetCurrentMenu();
    }
};

//---------------------------页签相关代码----------------------------//

/**
 * Creates a temporary graph instance for rendering off-screen content.
 */
myGraphEditorUI.prototype.isPagesEnabled = function()
{
    return this.editor.editable;
};

/**
 *
 */
myGraphEditorUI.prototype.setFileData = function()
{
    this.currentPage = null;
    this.fileNode = null;
    this.pages = null;

    var node = this.editor.graph.view.backgroundPageShape.node;
        if (node != null )
        {
                var selectedPage = null;
                this.fileNode = node;
                this.pages = [];


                    // Adds page ID based on page order to match
                    // remote IDs given if IDs are missing here
                    if (node.getAttribute('id') == null)
                    {
                        node.setAttribute('id', 0);
                    }

                    var page = new DiagramPage(node);
                    // Checks for invalid page names
                    if (page.getName() == null)
                    {
                        page.setName("拓扑图");
                    }
                    this.pages.push(page);
                     selectedPage = page;


                this.currentPage = (selectedPage != null) ? selectedPage :
                    this.pages[Math.max(0, Math.min(this.pages.length - 1,  0))];
                node = this.currentPage.node;

        }

        // Avoids duplicate parsing of the XML stored in the node
        if (this.currentPage != null)
        {
            this.currentPage.root = this.editor.graph.model.root;
        }
};



/**
 * Change types
 */
function ChangePageSetup(ui, color, image, format, pageScale)
{
    this.ui = ui;
    this.color = color;
    this.previousColor = color;
    this.image = image;
    this.previousImage = image;
    this.format = format;
    this.previousFormat = format;
    this.pageScale = pageScale;
    this.previousPageScale = pageScale;

    // Needed since null are valid values for color and image
    this.ignoreColor = false;
    this.ignoreImage = false;
}

/**
 * Implementation of the undoable page rename.
 */
ChangePageSetup.prototype.execute = function()
{
    var graph = this.ui.editor.graph;

    if (!this.ignoreColor)
    {
        this.color = this.previousColor;
        var tmp = graph.background;
        this.ui.setBackgroundColor(this.previousColor);
        this.previousColor = tmp;
    }

    if (!this.ignoreImage)
    {
        this.image = this.previousImage;
        var tmp = graph.backgroundImage;
        var img = this.previousImage;

        if (img != null && img.src != null && img.src.substring(0, 13) == 'data:page/id,')
        {
            img = this.ui.createImageForPageLink(img.src, this.ui.currentPage);
        }

        this.ui.setBackgroundImage(img);
        this.previousImage = tmp;
    }

    if (this.previousFormat != null)
    {
        this.format = this.previousFormat;
        var tmp = graph.pageFormat;

        if (this.previousFormat.width != tmp.width ||
            this.previousFormat.height != tmp.height)
        {
            this.ui.setPageFormat(this.previousFormat);
            this.previousFormat = tmp;
        }
    }

    if (this.foldingEnabled != null && this.foldingEnabled != this.ui.editor.graph.foldingEnabled)
    {
        this.ui.setFoldingEnabled(this.foldingEnabled);
        this.foldingEnabled = !this.foldingEnabled;
    }

    if (this.previousPageScale != null)
    {
        var currentPageScale = this.ui.editor.graph.pageScale;

        if (this.previousPageScale != currentPageScale)
        {
            this.ui.setPageScale(this.previousPageScale);
            this.previousPageScale = currentPageScale;
        }
    }
};

// Registers codec for ChangePageSetup
(function()
{
    var codec = new mxObjectCodec(new ChangePageSetup(),  ['ui', 'previousColor', 'previousImage', 'previousFormat', 'previousPageScale']);

    codec.afterDecode = function(dec, node, obj)
    {
        obj.previousColor = obj.color;
        obj.previousImage = obj.image;
        obj.previousFormat = obj.format;
        obj.previousPageScale = obj.pageScale;

        if (obj.foldingEnabled != null)
        {
            obj.foldingEnabled = !obj.foldingEnabled;
        }

        return obj;
    };

    mxCodecRegistry.register(codec);
})();