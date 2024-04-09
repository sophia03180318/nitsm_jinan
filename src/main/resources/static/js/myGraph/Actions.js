/**
 * Copyright (c) 2006-2020, JGraph Ltd
 * Copyright (c) 2006-2020, draw.io AG
 *
 * Constructs the actions object for the given UI.
 */
function Actions(editorUi) {
    this.editorUi = editorUi;
    this.actions = new Object();
    this.init();
};

/**
 * Adds the default actions.
 */
Actions.prototype.init = function () {
    var ui = this.editorUi;
    var editor = ui.editor;
    editor.actions=this.actions;
    var graph = editor.graph;
    var isGraphEnabled = function () {
        return Action.prototype.isEnabled.apply(this, arguments) && graph.isEnabled();
    };

    function deleteCells(includeEdges) {
        // Cancels interactive operations
        graph.escape();
        var cells = graph.getDeletableCells(graph.getSelectionCells());
        for(var i=0;i<cells.length;i++){
            var cell=cells[i];
            if(cell.isPort!=null&&cell.isPort==1&&cell.edges!=null){//如果是端口，将端口的连线一同删除
                cells=cells.concat(cell.edges);
            }
        }
        for(var i=0;i<cells.length;i++){
            var cell=cells[i];
            if(cell.edge==true){//如果是连线,将连线的对应的端口删除
                if(cell.source!=null){
                    cells.push(cell.source);
                }
               if(cell.target!=null){
                   cells.push(cell.target);
               }
            }

        }

        if (cells != null && cells.length > 0) {
            var parents = (graph.selectParentAfterDelete) ? graph.model.getParents(cells) : null;
            graph.removeCells(cells, includeEdges);

            // Selects parents for easier editing of groups
            if (parents != null) {
                var select = [];

                for (var i = 0; i < parents.length; i++) {
                    if (graph.model.contains(parents[i]) &&
                        (graph.model.isVertex(parents[i]) ||
                            graph.model.isEdge(parents[i]))) {
                        select.push(parents[i]);
                    }
                }

                graph.setSelectionCells(select);
            }
        }
    };

    this.addAction('delete', function (evt) {
        deleteCells(evt != null && mxEvent.isShiftDown(evt));
    }, null, null, 'Delete');
    this.addAction('deleteAll', function () {
        deleteCells(true);
    }, null, null, myGraphEditor.ctrlKey + '+Delete');

    // Edit actions
    this.addAction('undo', function () {
        ui.undo();
    });
    this.addAction('redo', function () {
        ui.redo();
    });

    this.addAction('zoomIn', function (evt) {
        graph.zoomIn();
    });
    this.addAction('zoomOut', function (evt) {
        graph.zoomOut();
    });
    this.addAction('toFront', function () {
        graph.orderCells(false);
    });
    this.addAction('toBack', function () {
        graph.orderCells(true);
    }, null, null, myGraphEditor.ctrlKey + 'Z');



    //备注编辑
    this.addAction('editorRemark', function(evt)
    {
       // style, width, height, value, title, showLabel, showTitle, tags
       //  'text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;',
       //      40, 20, 'Text', 'Text', null, null, 'text textbox textarea label'
       // var style='text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;';
        var width=100;
        var height=60;
        var value='请输入备注信息';
        var cells = [new mxCell((value != null) ? value : '', new mxGeometry(0, 0, width, height), 'editorMark')];
        cells[0].vertex = true;
        cells[0].setConnectable(false);
        cells = graph.getImportableCells(cells);
        graph.stopEditing();
        if (!graph.isCellLocked(graph.getDefaultParent()))
        {
            graph.model.beginUpdate();
                var pt = graph.getCenterInsertPoint(graph.getBoundingBoxFromGeometry(cells, true));
                x = Math.round(pt.x);
                y = Math.round(pt.y);
                select = graph.importCells(cells, x, y, null);
                // Executes parent layout hooks for position/order
                if (graph.layoutManager != null)
                {
                    var layout = graph.layoutManager.getLayout(null);
                    if (layout != null)
                    {
                        var s = graph.view.scale;
                        var tr = graph.view.translate;
                        var tx = (x + tr.x) * s;
                        var ty = (y + tr.y) * s;
                        for (var i = 0; i < select.length; i++)
                        {
                            layout.moveCell(select[i], tx, ty);
                        }
                    }
                }
                graph.model.endUpdate();
            if (select != null && select.length > 0)
            {
                graph.scrollCellToVisible(select[0]);
                graph.setSelectionCells(select);
            }
        }
    });


    //拓扑发现点击事件
    this.addAction('discoverTopo', function(evt)
    {
          var category=editor.category;
          if(category!=null&&category=="net_topo"){
              $.ajax({
                  url: "/api/find/begin",
                  type: "post",
                  data: {},
                  contentType: 'application/json;charset=utf-8',
                  success: function (result) {
                      if (result.code==200) {
                          if(result.data){
                              $.ajax({
                                  url: "/api/find/beginFind",
                                  type: "post",
                                  data:  null,
                                  contentType: 'application/json;charset=utf-8',
                                  success: function (result) {
                                      if (result.code==200) {
                                          layer.msg(result.msg);
                                      }
                                  }
                              });
                          }else{
                              openLayer= layer.open({
                                  closeBtn :0,
                                  btn: ['拓扑发现','取消']   ,
                                  content: "拓扑发现程序未启动",
                                  yes: function (index, layero) {
                                      $.ajax({
                                          url: "/api/find/beginFind",
                                          type: "post",
                                          data:  null,
                                          contentType: 'application/json;charset=utf-8',
                                          success: function (result) {
                                              if (result.code==200) {
                                                  layer.msg(result.msg);
                                              }
                                          }
                                      });
                                  }
                              });
                          }
                      }
                      return false;
                  }
              })




















          }else{
              layer.alert("请选中网络拓扑中的设备！");
          }

    });

    this.addAction('group', function(evt)
    {
        if (graph.isEnabled())
        {

            graph.setSelectionCell(editor.groupCells());
             //去掉之前vlan画框的功能
            // $.ajax({
            //     url: "/system/graphInterface/listPortVlan",
            //     type: "post",
            //     data:   graph.clickAssetId,
            //     contentType: 'application/json;charset=utf-8',
            //     success: function (result) {
            //         if (result.data) {
            //
            //             var content=  '<select name="range"  style="width:100%" id="range" lay-filter="range">';
            //             for(var i=0;i<result.data.length;i++){
            //                 content=content+  '<option value=\"'+result.data[i].portName+'\">'+result.data[i].portName+'</option>' ;
            //             }
            //
            //             content=content+'</select>';
            //
            //
            //             layer.open({
            //                 title: '请选择所属Vlan'
            //                 , btn: ['确定']
            //                 , content:content,
            //                 yes: function (index,layero) {
            //                     var vlanName=$('#range').val();
            //                     layer.close(index);
            //                     graph.setSelectionCell(editor.groupCells(vlanName));
            //                 }});
            //         }
            //     }
            // });
        }
    });


    this.addAction('groupCell', function(evt)
    {
        if (graph.isEnabled())
        {

            var content=  '<input name="groupCell"  style="width:100%" id="groupCell" lay-filter="groupCell">';
            layer.open({
                title: '请输入分组名称'
                , btn: ['确定']
                , content:content,
                yes: function (index,layero) {
                    var vlanName=$('#groupCell').val();
                    layer.close(index);
                    graph.setSelectionCell(editor.groupCells(vlanName));
                }});
        }
    });
    this.addAction('turn', function(evt)
    {
      // var selectionCells =  graph.getSelectionCells();
       var groupCell = graph.getSelectionCell();
       if(groupCell == null){
			alert('请选择组节点')
			return false;
		}
       if(groupCell.style.indexOf('group') != '-1'){   //归为一组在旋转，不需要每个都旋转
	       	var geo = graph.getCellGeometry(groupCell);
	        var geogeo = geo.clone();
		 //   geo.x = geo.width / 2 - geo.height / 2;
		 //   geo.y = geo.height / 2 - geo.width / 2;
		    var tmp = geo.width;
		    geogeo.x = geo.x;
		    geogeo.y = geo.y;
		    geogeo.width = geo.height;
		    geogeo.height = tmp;
		    console.log( geogeo.x + '+++++++')
		    // 读取当前90度的方向和样式
		  //  var state = groupCell.style.indexOf("rotation=") == '-1' ? 90 :  + 90
		 //   var state = 0
		    //旋转组节点
		  //  graph.setCellStyles(mxConstants.STYLE_ROTATION, state, [groupCell]);
		    graph.getModel().setGeometry(groupCell, geogeo);
		    var cells = groupCell.children
		    if(cells != null && cells.length > 0){
			    cells.forEach((elm,index) => {
				var state = null;
			 	if(elm.style.indexOf("rotation=") == '-1'){
					state = 90
				}else{
					if(elm.style.slice(elm.style.lastIndexOf("rotation=") + 9,elm.style.length - 1) == 90 || elm.style.slice(elm.style.lastIndexOf("rotation=") + 9,elm.style.length - 1) == 9){
						state = 0
					}else{
						state = 90
					}
				}
					var geoC = graph.getCellGeometry(elm);
			        var geogeoC = geoC.clone();
			        if(state == 90){
				        if(index%2 == 0){
							geoC.x = 53;
					    	geoC.y = 10  + 20*index;
						}else{
							geoC.x = 15;
					    	geoC.y = 10 + 20 * (index-1);
						}
					}else{
						if(index%2 == 0){
							geoC.x = 10 + 20*index;
					    	geoC.y = 5;
						}else if(index%2 == 1){
					    	geoC.x = 10 + 20*(index-1);
					    	geoC.y = 40;
						}
					}

				    var tmpC = geoC.width;
				    geogeoC.width = geoC.height;
				    geogeoC.height = tmpC;
				    geogeoC.x = geoC.x
				    geogeoC.y = geoC.y
				    graph.setCellStyles(mxConstants.STYLE_ROTATION, state, [elm]);
				    graph.getModel().setGeometry(elm, geogeoC);
				})
			}





		}else{
		   alert('请选择组节点进行旋转')
		}
    });

    this.addAction('ungroup', function(editor)
    {
        if (graph.isEnabled())
        {
            graph.setSelectionCells(graph.ungroupCells());
        }
    });

    // Defines a new action for deleting or ungrouping
    this.addAction('groupOrUngroup', function(evt,cell)
    {
        cell = cell || graph.getSelectionCell();
        if (cell != null && graph.isRectangle(cell))
        {
            editor.execute('ungroup', cell);
        }
        else
        {
            editor.execute('group');

        }
    });

    this.addAction('groupOrUngroupCell', function(evt,cell)
    {
        if(graph.getModel()!=null&&graph.getModel().cells["room"]!=null){
            graph.removeSelectionCell(graph.getModel().cells["room"]);
        }
        cell = cell || graph.getSelectionCell();
        if(cell!=null){

            if (graph.isRectangle(cell))
            {
                var flag=true;
                var selectionCells =  graph.getSelectionCells();
                for(var i=0;i<selectionCells.length;i++){
                    var onlyCell=selectionCells[i];
                    if(!graph.isRectangle(cell)){
                        flag=false;
                    }
                }
                if(flag&&selectionCells.length>1){

                    editor.execute('groupCell');//如果存在一个不是group的，就要做ungroup操作

                }else{
                    editor.execute('ungroup', cell);
                }

            }
            else
            {
                if(graph.getModel()!=null&&graph.getModel().cells["room"]!=null){
                    graph.removeSelectionCell(graph.getModel().cells["room"]);
                }
                editor.execute('groupCell');

            }
        }

    });
    // 保存图形
    this.addAction('save', function () {
        this.saveGraph(editor,ui); // 保存图形
    });
    this.addAction('outline', function () {
        if (this.outlineWindow == null) {
            // LATER: Check layers window for initial placement
            this.outlineWindow = new OutlineWindow(ui, document.body.offsetWidth - 260, 100, 180, 180);
            this.outlineWindow.window.addListener('show', function () {
                ui.fireEvent(new mxEventObject('outline'));
            });
            this.outlineWindow.window.addListener('hide', function () {
                ui.fireEvent(new mxEventObject('outline'));
            });
            this.outlineWindow.window.setVisible(true);
            ui.fireEvent(new mxEventObject('outline'));
        } else {
            this.outlineWindow.window.setVisible(!this.outlineWindow.window.isVisible());
        }
    });

    // 直线连接
    this.addAction('straightLine', function (evt) {
        var contain = editor.editorStatus;
        if (contain.childNodes != null && contain.childNodes.length > 0) {
            for (var i = 0; i < contain.childNodes.length; i++) {
                var button = contain.childNodes[i];
                button.style.border = 'none';
            }
        }
        var button = evt.currentTarget;
        button.style.border = '1px solid #000000';
        var style = graph.defaultEdgeStyle;
        style['edgeStyle'] = 'none';
        style['dashed']=0;
        graph.currentEdgeStyle = mxUtils.clone(graph.defaultEdgeStyle);
        ui.fireEvent(new mxEventObject('styleChanged', 'keys', [], 'values', [], 'cells', []));
    });
    // 折线连接
    this.addAction('verticalLine', function (evt) {
        var contain = editor.editorStatus;
        if (contain.childNodes != null && contain.childNodes.length > 0) {
            for (var i = 0; i < contain.childNodes.length; i++) {
                var button = contain.childNodes[i];
                button.style.border = 'none';
            }
        }
        var button = evt.currentTarget;
        button.style.border = '1px solid #000000';
        var style = graph.defaultEdgeStyle;
        style['edgeStyle'] = 'wireEdgeStyle';
        style['dashed']=0;
        graph.currentEdgeStyle = mxUtils.clone(graph.defaultEdgeStyle);
        ui.fireEvent(new mxEventObject('styleChanged', 'keys', [], 'values', [], 'cells', []));
    });



    // 直线虚线
    this.addAction('straightLineDashed', function (evt) {
        var contain = editor.editorStatus;
        if (contain.childNodes != null && contain.childNodes.length > 0) {
            for (var i = 0; i < contain.childNodes.length; i++) {
                var button = contain.childNodes[i];
                button.style.border = 'none';
            }
        }
        var button = evt.currentTarget;
        button.style.border = '1px solid #000000';
        var style = graph.defaultEdgeStyle;
        style['edgeStyle'] = 'none';
        style['dashed']=1;
        mxUtils.clone(graph.defaultEdgeStyle);
        graph.currentEdgeStyle = mxUtils.clone(graph.defaultEdgeStyle);
        ui.fireEvent(new mxEventObject('styleChanged', 'keys', [], 'values', [], 'cells', []));
    });
    // 折线虚线
    this.addAction('verticalLineDashed', function (evt) {
        var contain = editor.editorStatus;
        if (contain.childNodes != null && contain.childNodes.length > 0) {
            for (var i = 0; i < contain.childNodes.length; i++) {
                var button = contain.childNodes[i];
                button.style.border = 'none';
            }
        }
        var button = evt.currentTarget;
        button.style.border = '1px solid #000000';
        var style = graph.defaultEdgeStyle;
        style['edgeStyle'] = 'wireEdgeStyle';
        style['dashed']=1;
        graph.currentEdgeStyle = mxUtils.clone(graph.defaultEdgeStyle);
        ui.fireEvent(new mxEventObject('styleChanged', 'keys', [], 'values', [], 'cells', []));
    });


};

/**
 * Registers the given action under the given name.
 */
Actions.prototype.addAction = function (key, funct, enabled, iconCls, shortcut) {
    var title;
    if (key.substring(key.length - 3) == '...') {
        key = key.substring(0, key.length - 3);
        title = mxResources.get(key) + '...';
    } else {
        title = mxResources.get(key);
    }

    return this.put(key, new Action(title, funct, enabled, iconCls, shortcut));
};

/**
 * Registers the given action under the given name.
 */
Actions.prototype.put = function (name, action) {
    this.actions[name] = action;

    return action;
};

/**
 * Returns the action for the given name or null if no such action exists.
 */
Actions.prototype.get = function (name) {
    return this.actions[name];
};



/**
 * Constructs a new action for the given parameters.
 */
function Action(label, funct, enabled, iconCls, shortcut) {
    mxEventSource.call(this);
    this.label = label;
    this.funct = this.createFunction(funct);
    this.enabled = (enabled != null) ? enabled : true;
    this.iconCls = iconCls;
    this.shortcut = shortcut;
    this.visible = true;
};
function getFinishFlag(assetIds,graph){
	$.ajax({
	  url: "/api/find/getAssetTopoFindStatus",
      type: "post",
      data: JSON.stringify({ assetIdList:assetIds}),
      contentType: 'application/json;charset=utf-8',
      success: function (result) {
      if (result.code==200) {
       	var data = result.data
       	data.forEach(elm => {
			if(elm.findFinishFlag == '1'){
				var allData = graph.getModel().cells;
				Object.keys(allData).forEach(function(key) {
					if (allData[key].assetId == elm.assetId) {

						graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, '#2daf16', [graph.getModel().cells[allData[key].id]]);
					}
				})
			}
		})
      }
     }
 })
}

// Action inherits from mxEventSource
mxUtils.extend(Action, mxEventSource);

/**
 * Sets the enabled state of the action and fires a stateChanged event.
 */
Action.prototype.createFunction = function (funct) {
    return funct;
};

/**
 * Sets the enabled state of the action and fires a stateChanged event.
 */
Action.prototype.setEnabled = function (value) {
    if (this.enabled != value) {
        this.enabled = value;
        this.fireEvent(new mxEventObject('stateChanged'));
    }
};

/**
 * Sets the enabled state of the action and fires a stateChanged event.
 */
Action.prototype.isEnabled = function () {
    return this.enabled;
};

Action.prototype.saveGraph = function (value, category) {
};

Action.prototype.execute = function (actionname, cell, evt) {
    var action = editor.actions[actionname];
    if (action != null) {
        try {
            // Creates the array of arguments by replacing the actionname
            // with the editor instance in the args of this function
            var args = arguments;
            args[0] = this;

            // Invokes the function on the editor using the args
            action.apply(this, args);
        } catch (e) {
            mxUtils.error('Cannot execute ' + actionname +
                ': ' + e.message, 280, true);

            throw e;
        }
    } else {
        mxUtils.error('Cannot find action ' + actionname, 280, true);
    }
};


var OutlineWindow = function(editorUi, x, y, w, h)
{
    var graph = editorUi.editor.graph;

    var div = document.createElement('div');
    div.style.position = 'absolute';
    div.style.width = '100%';
    div.style.height = '100%';
    div.style.border = '1px solid whiteSmoke';
    div.style.overflow = 'hidden';

    this.window = new mxWindow(mxResources.get('outline'), div, x, y, w, h, true, true);
    this.window.minimumSize = new mxRectangle(0, 0, 80, 80);
    this.window.destroyOnClose = false;
    this.window.setMaximizable(false);
    this.window.setResizable(true);
    this.window.setClosable(true);
    this.window.setVisible(true);

    this.window.setLocation = function(x, y)
    {
        var iw = window.innerWidth || document.body.clientWidth || document.documentElement.clientWidth;
        var ih = window.innerHeight || document.body.clientHeight || document.documentElement.clientHeight;

        x = Math.max(0, Math.min(x, iw - this.table.clientWidth));
        y = Math.max(0, Math.min(y, ih - this.table.clientHeight - 48));

        if (this.getX() != x || this.getY() != y)
        {
            mxWindow.prototype.setLocation.apply(this, arguments);
        }
    };

    var resizeListener = mxUtils.bind(this, function()
    {
        var x = this.window.getX();
        var y = this.window.getY();

        this.window.setLocation(x, y);
    });

    mxEvent.addListener(window, 'resize', resizeListener);

    var outline = editorUi.createOutline(this.window);

    this.destroy = function()
    {
        mxEvent.removeListener(window, 'resize', resizeListener);
        this.window.destroy();
        outline.destroy();
    }

    this.window.addListener(mxEvent.RESIZE, mxUtils.bind(this, function()
    {
        outline.update(false);
        outline.outline.sizeDidChange();
    }));

    this.window.addListener(mxEvent.SHOW, mxUtils.bind(this, function()
    {
        this.window.fit();
        outline.suspended = false;
        outline.outline.refresh();
        outline.update();
    }));

    this.window.addListener(mxEvent.HIDE, mxUtils.bind(this, function()
    {
        outline.suspended = true;
    }));

    this.window.addListener(mxEvent.NORMALIZE, mxUtils.bind(this, function()
    {
        outline.suspended = false;
        outline.update();
    }));

    this.window.addListener(mxEvent.MINIMIZE, mxUtils.bind(this, function()
    {
        outline.suspended = true;
    }));

    var outlineCreateGraph = outline.createGraph;
    outline.createGraph = function(container)
    {
        var g = outlineCreateGraph.apply(this, arguments);
        g.gridEnabled = false;
        g.pageScale = graph.pageScale;
        g.pageFormat = graph.pageFormat;
        g.background = (graph.background == null || graph.background == mxConstants.NONE) ? graph.defaultPageBackgroundColor : graph.background;
        g.pageVisible = graph.pageVisible;

        var current = mxUtils.getCurrentStyle(graph.container);
        div.style.backgroundColor = current.backgroundColor;

        return g;
    };

    function update()
    {
        outline.outline.pageScale = graph.pageScale;
        outline.outline.pageFormat = graph.pageFormat;
        outline.outline.pageVisible = graph.pageVisible;
        outline.outline.background = (graph.background == null || graph.background == mxConstants.NONE) ? graph.defaultPageBackgroundColor : graph.background;;

        var current = mxUtils.getCurrentStyle(graph.container);
        div.style.backgroundColor = current.backgroundColor;

        if (graph.view.backgroundPageShape != null && outline.outline.view.backgroundPageShape != null)
        {
            outline.outline.view.backgroundPageShape.fill = graph.view.backgroundPageShape.fill;
        }

        outline.outline.refresh();
    };

    outline.init(div);

    editorUi.editor.addListener('resetGraphView', update);
    editorUi.addListener('pageFormatChanged', update);
    editorUi.addListener('backgroundColorChanged', update);
    editorUi.addListener('backgroundImageChanged', update);
    editorUi.addListener('pageViewChanged', function()
    {
        update();
        outline.update(true);
    });

    if (outline.outline.dialect == mxConstants.DIALECT_SVG)
    {
        var zoomInAction = editorUi.actions.get('zoomIn');
        var zoomOutAction = editorUi.actions.get('zoomOut');

        mxEvent.addMouseWheelListener(function(evt, up)
        {
            var outlineWheel = false;
            var source = mxEvent.getSource(evt);

            while (source != null)
            {
                if (source == outline.outline.view.canvas.ownerSVGElement)
                {
                    outlineWheel = true;
                    break;
                }

                source = source.parentNode;
            }

            if (outlineWheel)
            {
                if (up)
                {
                    zoomInAction.funct();
                }
                else
                {
                    zoomOutAction.funct();
                }
            }
        });
    }
};

