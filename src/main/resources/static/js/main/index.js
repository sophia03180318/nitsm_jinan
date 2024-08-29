// 程序启动方法
(function() {
	var editor = new myGraphEditor(false);
	// 底部按钮
	var bottomButtons = [
		// 删除
		{
			'title': '删除(快捷键Delete)',
			'action': 'delete',
			'label': '',
			'img': '/graph/images/icon/delete.png',
			'select': false,
			'isTransparent': true
		},
		// 撤消
		{
			'title': '撤销(快捷键Ctrl+Z)',
			'action': 'undo',
			'label': '',
			'img': '/graph/images/icon/undo.png',
			'select': false,
			'isTransparent': true
		},
		// 还原
		{
			'title': '还原',
			'action': 'redo',
			'label': '',
			'img': '/graph/images/icon/redo.png',
			'select': false,
			'isTransparent': true
		},
		// 放大
		{
			'title': '放大',
			'action': 'zoomIn',
			'label': '',
			'img': '/graph/images/icon/zoomin.png',
			'select': false,
			'isTransparent': true
		},
		// 缩小
		{
			'title': '缩小',
			'action': 'zoomOut',
			'label': '',
			'img': '/graph/images/icon/zoomout.png',
			'select': false,
			'isTransparent': true
		},
		{
			'title': '保存',
			'action': 'save',
			'label': '',
			'img': '/graph/images/icon/save.png',
			'select': false,
			'isTransparent': true
		},
		{
			'title': '直线',
			'action': 'straightLine',
			'label': '',
			'img': '/graph/images/icon/straight.gif',
			'select': false,
			'isTransparent': true
		}
		,
		{
			'title': '折线',
			'action': 'verticalLine',
			'label': '',
			'img': '/graph/images/icon/vertical.gif',
			'select': true,
			'isTransparent': true
		},
		{
			'title': '直线(虚线)',
			'action': 'straightLineDashed',
			'label': '',
			'img': '/graph/images/icon/arrow.gif',
			'select': false,
			'isTransparent': true
		}
		,
		{
			'title': '折线(虚线)',
			'action': 'verticalLineDashed',
			'label': '',
			'img': '/graph/images/icon/entity.gif',
			'select': false,
			'isTransparent': true
		},
		{
			'title': '轮廓',
			'action': 'outline',
			'label': '',
			'img': '/graph/images/icon/outline.gif',
			'select': false,
			'isTransparent': true
		},
		{
			'title': '(Un)group',
			'action': 'groupOrUngroupCell',
			'label': '',
			'img': '/graph/images/icon/group.png',
			'select': false,
			'isTransparent': true
		},
		{
			'title': '置于上层',
			'action': 'toFront',
			'label': '',
			'img': '/graph/images/icon/tofront.gif',
			'select': false,
			'isTransparent': true
		},
		{
			'title': '置于下层',
			'action': 'toBack',
			'label': '',
			'img': '/graph/images/icon/toback.gif',
			'select': false,
			'isTransparent': true
		},
		{
			'title': '拓扑发现',
			'action': 'discoverTopo',
			'label': '',
			'img': '/graph/images/icon/collapse.gif',
			'select': false,
			'isTransparent': true
		},
		{
			'title': '备注编辑',
			'action': 'editorRemark',
			'label': '',
			'img': '/graph/images/edit.gif',
			'select': false,
			'isTransparent': true
		},



	];

	//创建核心展示界面
	myGraphEditorUI.prototype.init = function () {
		var graph = this.editor.graph;
		graph.setAllowDanglingEdges(false);
		//创建页面Div,diagramContainer
		this.createDivs();
		// 禁用浏览器默认的右键菜单栏
		mxEvent.disableContextMenu(this.diagramContainer);
		//初始化graph
		graph.init(this.diagramContainer);
		this.refresh();
		this.editor.resetGraph();
		this.initCanvas();
		this.pages=[];
		this.tabContainer=this.createTabContainer();
		this.container.appendChild(this.tabContainer) ;//创建页签
		this.tabContainer.appendChild(this.createPageMenuTab()) ;//创建pages工具
		this.setFileData();//初始化页签memu
		graph.sizeDidChange();
		this.initPages();
		//插入面板
		$('#room2').on('click','li',function(type){
			var currentPage = editorUi.pages[0];
			editorUi.selectPage(currentPage);
			if(graph.getModel().cells["room"]==null){
				viewGraph(graph, editor, currentPage,true);
			}else{
				layer.alert("平面图已经存在，无需配置");
			}




		});

		// Global handler to hide the current menu   页面菜单显示隐藏
		this.gestureHandler = mxUtils.bind(this, function(evt)
		{
			this.hideCurrentMenu();
		});
		mxEvent.addGestureListeners(document, this.gestureHandler);
		// 在窗口底部添加按钮
		if (this.bottomButtons != null) {
			status.innerHTML = "";
			for (var i = 0; i < this.bottomButtons.length; i++) {
				var btn = this.bottomButtons[i];
				var action = this.actions.get(btn.action);
				if (action != null) {
					this.addToolbarButton(this.editor, this.editor.editorStatus, action, btn.label,
						btn.img, btn.isTransparent, btn.title, btn.select);
				}
			}
		}
		;
	}


	var editorUi = new myGraphEditorUI(editor, document.getElementById('topoContainer'), 'net_topo', document.getElementById('statusContainer'), bottomButtons);

	var graph = editor.graph;
	//设置默认组
	var style = new Object();
	style[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_RECTANGLE;
	style[mxConstants.STYLE_PERIMETER] = mxPerimeter.RectanglePerimeter;
	style[mxConstants.STYLE_ALIGN] = mxConstants.ALIGN_CENTER;
	style[mxConstants.STYLE_VERTICAL_ALIGN] = mxConstants.ALIGN_TOP;
	style[mxConstants.STYLE_GRADIENTCOLOR] = '#F8C48B';
	style[mxConstants.STYLE_STROKECOLOR] = '#E86A00';
	style[mxConstants.STYLE_FONTCOLOR] = '#000000';
	style[mxConstants.STYLE_FILLCOLOR] = '#FF9103';
	style[mxConstants.STYLE_ROUNDED] = true;
	style[mxConstants.STYLE_OPACITY] = '10';
	style[mxConstants.STYLE_STARTSIZE] = '30';
	style[mxConstants.STYLE_FONTSIZE] = '10';
	style[mxConstants.STYLE_FONTSTYLE] = 1;
	graph.getStylesheet().putCellStyle('group', style);

	var bgStyle = new Object();
	bgStyle[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_IMAGE
	bgStyle[mxConstants.STYLE_RESIZABLE] = '0'
	bgStyle[mxConstants.STYLE_FONTSIZE] = '0';
	graph.getStylesheet().putCellStyle('bgStyle', bgStyle);

	//groupBorderSize
	//设置图形和它的子元素的边距
	var group = new mxCell('', new mxGeometry(), 'group');
	group.setVertex(true);
	group.setConnectable(false);
	editor.defaultGroup = group;
	editor.groupBorderSize = 20;
    	//右击
	graph.popupMenuHandler.factoryMethod = function(menu, cell, evt)
    {
    	if(cell==null){
			menu.addItem('刷新', null, function()  {
				var currentPage=editorUi.currentPage;
				viewGraph(graph, editor, currentPage);
			})
		}
	    else  if(cell.vertex && cell.assetType == "net_topo" && cell.isPort != '1'){
	  	 var selectpage=editorUi.getPageById(cell.assetId);
	  	 if(selectpage!=null){
			 menu.addItem('打开设备连线信息', null, function()  {
				 editorUi.selectPage(selectpage);
				 graph.fireEvent(new mxEventObject(mxEvent.DOUBLE_CLICK, 'cell', cell));
			 })
		 }else{
			 menu.addItem('设备连线信息', null, function()
			 {
				 editorUi.updateTabContainer(cell.value,cell.assetId);
				 var currentPage=editorUi.currentPage;
				 currentPage.category="netWorkAsset_topo";
				 viewGraph(graph, editor, currentPage);
				 graph.fireEvent(new mxEventObject(mxEvent.DOUBLE_CLICK, 'cell', cell));
			 })
		 }

      };
    }
	graph.addListener(mxEvent.DOUBLE_CLICK, function(sender, evt) {
		//拓扑图双机事件，主要用来配置端口信息

		var cell = evt.getProperty('cell');
		if (cell != null && cell.isPort == 1) {//双机端口操作
			$.ajax({
				url: "/system/graph/queryPortIndex",
				type: "post",
				data: cell.parent.assetId,
				contentType: 'application/json;charset=utf-8',
				success: function(result) {
					if (result.data) {
						var content = '<select name="portIndex"  style="width:100%" id="portIndex" lay-filter="portIndex">';
						for (var i = 0; i < result.data.length; i++) {
							if (cell.portIndex != null && cell.portIndex != "" && result.data[i].portIndex == cell.portIndex) {
								content = content + '<option     selected = "selected" value=\"' + result.data[i].portIndex + '\">' +
									(result.data[i].isPort == null ? '' : '[已配置]') +
									result.data[i].portIndex + '</option>';

							} else {
								content = content + '<option  value=\"' + result.data[i].portIndex + '\">' +
									(result.data[i].isPort == null ? '' : '[已配置]') +
									result.data[i].portIndex + '</option>';

							}

						}

						content = content + '</select>';


						layer.open({
							title: '请选择端口索引'
							, btn: ['确定']
							, content: content,
							yes: function(index, layero) {
								var vlanName = $('#portIndex').val();
								var model = graph.getModel();
								model.beginUpdate();
								try {
									cell.setPortIndex(vlanName);
									graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, 'yellow', [cell]);
									//   cell.setStyle("fontSize=9;shape=rect;resizable=0;strokeColor=#000000;fillColor=yellow");
								} finally {
									model.endUpdate();
								}
								layer.close(index);
							}
						});
					}
				}
			});
		}
		//双击了网络拓扑中设备节点  双击调用发现，
		if (cell != null && cell.assetType != null && cell.assetType == "net_topo" && cell.vertex == true&&cell.isPort!=1) {
			$('#tabTopo .layui-tab-title li').eq(1).addClass('layui-this').siblings().removeClass('layui-this');
			$('#tabTopo .layui-tab-item ').eq(1).addClass('layui-show').siblings().removeClass('layui-show');
			localAssetName = cell.value
			$.ajax({
				url: "/system/graph/queryAssetTarget",
				type: "post",
				data: cell.assetId ,
				contentType: 'application/json;charset=utf-8',
				success: function(result) {
					var assetList = result.data;
					var assetIdList = [];
					assetIdList.push(cell.assetId)
					if (assetList != null && assetList.length > 0) {
						var content = "<tr><td>设备端口</td><td>对端设备</td><td>对端设备端口</td></tr>";
						for (var i = 0; i < assetList.length; i++) {
							var name=assetList[i].localPortIndex.toLowerCase();
							if(name.includes("port-channel")){
								continue;
							}
							content += "<tr><td><span data-attr='" +
								assetList[i].localAssetId + "'>" + assetList[i].localPortIndex + "</span></td><td><span data-attr='" +
								assetList[i].targetAssetId + "'>" + (assetList[i].targetAssetName==null?"":assetList[i].targetAssetName)  + "</span></td>" +
								"<td>" + (assetList[i].targetPortIndex==null?"":assetList[i].targetPortIndex) + "</td>" +
								"</tr>";
						}
						$("#topo_link_content").html( "<tr><td colspan='3'  >【"+cell.value+"】连接信息</td></tr>"+content);
					} else {
						var content = "<tr><td>无连接信息</td></tr>";
						$("#topo_link_content").html(content);
					}
				}
			});
		}
	});

	//添加连线监听事件
	graph.connectionHandler.addListener(mxEvent.CONNECT, function(sender, evt) {
		var cell = evt.getProperty("cell");
		var sourceCell = cell.source;
		var targetCell = cell.target;
		//查询连线端口信息
		$.ajax({
			url: "/system/graph/queryPortIScorrect",
			type: "post",
			data: JSON.stringify({ localAssetId: sourceCell.parent.assetId, targetAssetId: targetCell.parent.assetId }),
			contentType: 'application/json;charset=utf-8',
			beforeSend: function() {
			},
			success: function(result) {
				if (result.data != null && result.data.localPortIndex != null && result.data.targetPortIndex != null) {
					if(result.data.localAssetId==sourceCell.parent.assetId){
						sourceCell.setPortIndex(result.data.localPortIndex);
						targetCell.setPortIndex(result.data.targetPortIndex);
					}else{
						sourceCell.setPortIndex(result.data.targetPortIndex);
						targetCell.setPortIndex(result.data.localPortIndex);
					}


					graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, 'yellow', [sourceCell, targetCell]);
				}
			}
		});



	});


	// 组织结构树
	initOrgTree(graph, editor,editorUi);

	// TAB切换
	$("#topo_ul .layui-tab-title li").on('click', function() {
		var category = $(this).attr("id");
		var currentPage = editorUi.pages[0];
		currentPage.category=category;
		editorUi.selectPage(currentPage);

		$('#roomContainer').empty()
		if(currentPage.category == 'cabinet_topo'){
			getRoomData(graph, editor, currentPage)
		}else{
			viewGraph(graph, editor, currentPage);
		}
	});

	$("#topo_link_content").on('click', 'tr', function(event) {
		var oCell = $(this)[0].children[0].children[0].dataset.attr;
		var sCell = $(this)[0].children[1].children[0].dataset.attr;

		var curCells = [];
		var allData = graph.getModel().cells;
		Object.keys(allData).forEach(function(key) {
			if (allData[key].assetId == oCell || allData[key].assetId == sCell) {
				curCells.push(graph.getModel().cells[allData[key].id]);
			}
		})

		graph.setSelectionCells(curCells); //选中节点

	});


})();
mxKeyHandler.prototype.isGraphEvent = function(evt) {
	return true;
};

Action.prototype.saveGraph = function(editor,ui) {
	var nodes = [];//拓扑中的节点
	var edges = [];//拓扑中的连线
	var points = [];//拓扑图中的端口
	var groups = [];//拓扑中的分组
	var marks = [];//拓扑中备注信息
	var pages = [];
	var currentPage=ui.currentPage;
	if (currentPage.category == null || editor.orgId == null) {
		layer.alert("未初始化拓扑图！");
		return;
	} else {
		var cells = editor.graph.getModel().cells;
		for (var key in cells) {
			var cell = cells[key];

			if (cell.assetId != null || (cell.edge != null && cell.edge == 1 && cell.source != null)) {
				if (cell.edge != null && cell.edge == 1) {
					console.log(cell)
					if(currentPage.category == "netWorkAsset_topo") {
						var edge = createEdge(cell, currentPage.category, editor.orgId,currentPage.getId());
					}else{
						var edge = createEdge(cell, currentPage.category, editor.orgId);
						console.log('走到了这里')
					}
					edge.portMode = edge.value
					edges.push(edge);
					if (cell.geometry.points != null) {
						if(currentPage.category == "netWorkAsset_topo"){
							createPoints(points, cell.geometry.points, cell.id, editor.orgId, currentPage.category,currentPage.getId());
						}else{
							createPoints(points, cell.geometry.points, cell.id, editor.orgId, currentPage.category);
						}

					}
				}
				if (cell.vertex != null && cell.vertex == 1) {
					var node = createNode(cell, currentPage.category, editor.orgId);
					if(currentPage.category == "netWorkAsset_topo"){
						node.coreAssetId=currentPage.getId();
					}
					nodes.push(node);
				}

			} else if ((cell.style != null && cell.style == "group")||cell.assetType=="group") {
				var enc = new mxCodec(mxUtils.createXmlDocument());
				var node = enc.encode(cell);
				var value = mxUtils.getXml(node);
				if(currentPage.category == "netWorkAsset_topo"){
					value.assetId=currentPage.getId();
				}
				groups.push(value);

			} else if (cell.style != null && cell.style == "editorMark") {
				var enc = new mxCodec(mxUtils.createXmlDocument());
				var node = enc.encode(cell);
				var value = mxUtils.getXml(node);
				if(currentPage.category == "netWorkAsset_topo"){
					value.assetId=currentPage.pageId;
				}
				marks.push(value);
			}
		}
	}

		if (nodes.length <= 0) {
			layer.alert('节点已经清空是否继续保存', {
				closeBtn: 1    // 是否显示关闭按钮
				, btn: ['确定', '关闭'] //按钮
				, icon: 6    // icon
				, yes: function() {
					if(currentPage.category == "netWorkAsset_topo"){
						saveTopoNodeAsset(nodes, edges, points, groups, marks,editor,currentPage);
					}else{
						saveTopoNode(nodes, edges, points, groups, marks,editor, currentPage);
					}

				}
				, btn2: function() {
					return;
				}
			});
		} else {
			if(currentPage.category == "netWorkAsset_topo"){
				saveTopoNodeAsset(nodes, edges, points, groups, marks,editor,currentPage);
			}else{
				saveTopoNode(nodes, edges, points, groups, marks,editor, currentPage);
			}


		}


};

function saveTopoNode(nodes, edges, points, groups, marks, editor,currentPage) {
	if(currentPage.category == 'cabinet_topo'){
		var v = { "nodes": nodes, "edges": edges, "points": points, "groups": groups, "marks": marks, "category": currentPage.category, "orgId": roomId };
	}else{
		var v = { "nodes": nodes, "edges": edges, "points": points, "groups": groups, "marks": marks, "category": currentPage.category, "orgId": editor.orgId , };
	}

	console.log(v)
	$.ajax({
		url: "/api/v2/graph/saveTopoNode",
		type: "post",
		data: JSON.stringify(v),
		contentType: 'application/json;charset=utf-8',
		success: function(result) {
			if (result.msg) {
				layer.alert(result.msg);
			} else {
				layer.alert("权限不足");
			}
		}
	});
}

function saveTopoNodeAsset(nodes, edges, points, groups, marks, editor,currentPage) {
	var v = { "nodes": nodes, "edges": edges, "points": points, "groups": groups, "marks": marks, "category": currentPage.category, "orgId": editor.orgId ,"assetId":currentPage.getId()};
	$.ajax({
		url: "/system/graph/saveNetWorkTopoNode",
		type: "post",
		data: JSON.stringify(v),
		contentType: 'application/json;charset=utf-8',
		success: function(result) {
			if (result.msg) {
				layer.alert(result.msg);
			} else {
				layer.alert("权限不足");
			}
		}
	});
}

function initOrgTree(graph, editor,editorUi) {
	var setting = {
		data: {
			simpleData: {
				enable: true
			}
		},
		check: {
			enable: true,
			chkboxType: { "Y": "", "N": "" }
		},
		callback: {
			onClick: function(event, treeId, treeNode) {
				var category = $("#topo_ul .layui-this").attr("id");
				editor.orgId = treeNode.id
				var currentPage = editorUi.pages[0]
				currentPage.category=category;
				editorUi.selectPage(currentPage);
				$('#roomContainer').empty()
				if(currentPage.category == 'cabinet_topo'){
					getRoomData(graph, editor, currentPage)
				}else{
					viewGraph(graph, editor, currentPage);
				}

			}
		}
	};
	$.get($("#orgTree").data("url"), function(result) {
		var zNodes = [];
		result.data.forEach(function(item) {
			var org = {
				id: item.id,
				pId: item.pid,
				name: item.title
			};
			if (item.pid === "0" || item.pid === "1") {
				org.open = true;
				org.checked = true;
			}
			zNodes.push(org);
		});
		$.fn.zTree.init($("#orgTree"), setting, zNodes);
	});
}
//获取组织下机房
var editorCabinet = null
var graphCabinet = null
var cabinetCurrentPage = null
var roomId = ''
var roomData = []
function getRoomData (graph, editor, currentPage) {
	$.ajax({
		url: "/api/room/list/" + editor.orgId,
		type: "get",
		contentType: 'application/json;charset=utf-8',
		success: function(result) {
			roomData = result.data
			if(roomData.length == 0){
				editor.graph.model.clear();
				return false
			}
			$('#roomContainer').empty()
			roomData.forEach((elm,index) => {
				if(index == 0){
					$('#roomContainer').append(
						'<li class="item pointer layui-this" onclick="changeRoom('+index+')">'+elm.name+'</li>'
					)
				}else{
					$('#roomContainer').append(
						'<li class="item pointer" onclick="changeRoom('+index+')">'+elm.name+'</li>'
					)
				}

			})
			roomId = roomData[0].id
			editorCabinet = editor
			graphCabinet = graph
			cabinetCurrentPage  = currentPage
			viewGraph(graph, editor, currentPage)
		}
	})
}




// 图形回显
function viewGraph(graph, editor, currentPage,isShowRoom) {
	if(currentPage.category == 'cabinet_topo'){
		var v = { "orgId": editor.orgId,roomId: roomId, "category": currentPage.category,"assetId":currentPage.getId() };
	}else{
		var v = { "orgId": editor.orgId, "category": currentPage.category,"assetId":currentPage.getId() };
	}
	$.ajax({
		url: "/api/v2/graph/topoNode",
		type: "post",
		data: JSON.stringify(v),
		contentType: 'application/json;charset=utf-8',
		success: function(result) {
			//如果是机房
			// cabinetAllMap = result.data
			// graphCabinet = graph
			// editorCabinet = editor
			// cabinetCurrentPage = currentPage

			// 清空画布
			editor.graph.model.clear();
			 // editor.resetGraph();
			// 在一个事务中添加所有cell到model中
			try {
				graph.getModel().beginUpdate();
				if(isShowRoom==true){
					showRoom(editor, graph);
				}
				if (result.data) {
					//测试使用手动添加
					var parent = graph.getDefaultParent();

					var map = result.data;
					if (map["groups"] != null && map["groups"].length > 0) {//开始构造组信息
						showGroup(map["groups"], editor, graph, parent,isShowRoom);
					}
					if (map["marks"] != null && map["marks"].length > 0) {//开始构造标注信息
						showMark(map["marks"], editor, graph, parent);
					}

					if (map["vertex"] != null && map["vertex"].length > 0) {//开始构造设备节点
						showNodes(map["vertex"], editor, graph, parent,currentPage);

					}

					if (map["edge"] != null && map["edge"].length > 0) {//开始构造连线信息
						showEdge(map["edge"], map["points"], editor, graph, parent)
					}
				}
				$('#topoContainer').trigger('click');
			} finally {
				// 更新图
				graph.getModel().endUpdate();
			}
		 }
	  });
}
function changeRoom (index) {
	$('#roomContainer .layui-tab-title li').eq(index).addClass('layui-this').siblings().removeClass('layui-this');
	$('#roomContainer .layui-tab-item ').eq(index).addClass('layui-show').siblings().removeClass('layui-show');
	roomId = roomData[index].id


	viewGraph(graphCabinet, editorCabinet, cabinetCurrentPage)

}
//显示分组信息
function showGroup(groups, editor, graph, parent,isShowRoom) {
	var model = graph.getModel();
	for (var i = 0; i < groups.length; i++) {
		var xmlDoc = mxUtils.parseXml(groups[i].contentStr);
		var codec = new mxCodec(xmlDoc);
		var group = new mxCell('Group', new mxGeometry(), 'group');
		group.setVertex(true);
		group.setConnectable(false);
		var parent = xmlDoc.documentElement.getAttribute('parent');
		var groupNode = codec.decode(xmlDoc.documentElement, group);
		if(group.value=="group"){
			group.value='';
		}
		if(!(group.id=="room"&&isShowRoom==true)){
		if (parent != null && parent != 1) {
			var nodeParent = graph.getModel().cells[parent];
			graph.addCell(groupNode, nodeParent);
		} else {
			graph.addCell(groupNode);
		}

		}

	}



}
//显示备注信息
function showMark(marks, editor, graph, parent) {
	var model = graph.getModel();

	for (var i = 0; i < marks.length; i++) {
		var xmlDoc = mxUtils.parseXml(marks[i].contentStr);
		var codec = new mxCodec(xmlDoc);
		var mark = new mxCell('mark', new mxGeometry(), 'editorMark');
		mark.setVertex(true);
		mark.setConnectable(false);
		var parent = xmlDoc.documentElement.getAttribute('parent');
		var markNode = codec.decode(xmlDoc.documentElement, mark);
		if (parent != null && parent != 1) {
			var nodeParent = graph.getModel().cells[parent];

			graph.addCell(markNode, nodeParent);
		} else {
			graph.addCell(markNode);
		}

	}

}



//显示节点
function showRoom(editor, graph) {
	var style  = "image=/graph/images/room.jpg";
		var id = "room";
			var x = graph.pageFormat.width / 2 -200
			var y = graph.pageFormat.height / 2 -200
			var width = 1465;
			var height = 732;
		var assetType = "group";
		var relative = false;
		var connectable = false;
	var parent = graph.getDefaultParent();
	var cell=graph.createVertexAsset(parent, id, null, x, y, width, height, style, relative, null, assetType, connectable, null, null, null);
	graph.setSelectionCells([cell]);
	graph.orderCells(true);
}

//显示节点
function showNodes(vertexs, editor, graph, parent,currentPage) {
	var style = graph.getStylesheet().styles.group;
	if (style != null) {
		style[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_CLOUD;
	}

	var cellStyle = graph.getModel().getStyle(editor.defaultGroup);
	var cells = editor.graph.getModel().cells;
	var num = 0;
	var flag = 1;
	for (var i = 0; i < vertexs.length; i++) {
		var vertex = vertexs[i];
		var id = vertex.nodeId;
		var value = vertex.assetName;

		var x = vertex.nodeX == null ? 55 * num : vertex.nodeX;

		if (vertex.nodeX == null && currentPage.category !== 'cabinet_topo') {
			num++;
		}
		if(vertex.rowIndex >1 ){
			var y = vertex.nodeY == null ? 55 * vertex.rowIndex : vertex.nodeY;
		}else{
			var y = vertex.nodeY == null ? 55 : vertex.nodeY;
		}
		if(vertex.nodeX == null && currentPage.category == 'netWorkAsset_topo'){
			var centerX = graph.pageFormat.width / 2
			var centerY = graph.pageFormat.height / 2 -100
			if(vertex.assetId == currentPage.getId()){
				x = centerX
				y = centerY
			}else{
				var xNum = 1;
				if(i % 2 == 1 ){  //所有节点根据索引。奇数放下面，偶数放上面
					x = centerX - xNum * 100 * flag
					y = centerY - 150
				}else{
					x = centerX - xNum * 100 * flag
					y = centerY + 150
					flag = flag == -1 ? 1 : -1
					xNum = xNum ++
				}
			}
		}
		if (currentPage.category == "cabinet_topo") {
			if(graph.getModel().cells["room"]!=null){
				var width = vertex.nodeWidth == null||vertex.nodeWidth<80 ? 80 : vertex.nodeWidth;
				var height = vertex.nodeHeight == null||vertex.nodeWidth ==80 ? 32 : vertex.nodeHeight;
			}else{
				style[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_RECTANGLE;

				var width = vertex.nodeWidth == null ? 108 : vertex.nodeWidth;
				var height = vertex.nodeHeight == null ? 26 : vertex.nodeHeight;
			}

		} else if ((currentPage.category == "pc_topo")) {
			if (style != null) {
				style[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_RECTANGLE;
			}
			var width = vertex.nodeWidth == null ? 70 : vertex.nodeWidth;
			var height = vertex.nodeHeight == null ? 70 : vertex.nodeHeight;
		} else {
			var width = vertex.nodeWidth == null ? 42 : vertex.nodeWidth;
			var height = vertex.nodeHeight == null ? 42 : vertex.nodeHeight;
		}

		var assetId = vertex.assetId;
		var assetType = currentPage.category;
		var relative = false;
		var connectable = false;
		var offset = null;
		var isPort = vertex.isPort;
		var portIndex = vertex.portIndex;
		if ((currentPage.category == "net_topo" || currentPage.category == "netWorkAsset_topo")&& isPort == null) {
			connectable = true;
		}
		var style = vertex.nodeStyle;
		if (vertex.nodeStyle == null) {
			if (vertex.assetMode == 201) {
				style = "image=/graph/images/picture/switch.png;spacingBottom=15px;";
			}
			if ( vertex.assetMode == 42) {
				style = "image=/graph/images/picture/router.png;spacingBottom=15px;";
			}
			if ( vertex.assetMode == 183) {
				style = "image=/graph/images/picture/serve.png;spacingBottom=15px;";
			}
			if(vertex.assetMode == 318){
				style = "image=/graph/images/picture/raid.png;spacingBottom=15px;";
			}
			if (currentPage.category == "cabinet_topo") {
				if(graph.getModel().cells["room"]!=null){
					style = "image=/graph/images/roomCabinet.jpg;verticalLabelPosition=center";
					x = vertex.nodeX == null ? 80 * (vertex.columnIndex - 1) : x;
					y = vertex.nodeY == null ? (vertex.rowIndex - 1) * 32 : y;
				}else{
					style = "shape=RECTANGLE;fillColor=rgba(40, 122, 212, 0.3);strokeColor=#00acff;fontColor=#00ACFF;fontStyle=1;rounded=1;verticalLabelPosition=center";
					x = vertex.nodeX == null ? 108 * (vertex.rowIndex - 1) + vertex.rowIndex * 30 : x;
					console.log(x)
					y = vertex.nodeY == null ? (vertex.columnIndex - 1) * 26 + 30 : y;
				}


			}
			if (currentPage.category == "pc_topo") {
				var xNew = 25 + 85 * ((i) % 16);
				x = vertex.nodeX == null ? xNew : x;
				var yNew = Math.floor((i) / 16) * 120;
				y = vertex.nodeY == null ? yNew : y;
				style = "image=/graph/images/picture/server.png;spacingBottom=15px";
			}
		} else {
			if( isPort != 1){
				if (currentPage.category == "net_topo" && vertex.assetMode == 201) {
					style = "image=/graph/images/picture/switch.png;spacingBottom=15px";
				}
				if (currentPage.category == "net_topo" && vertex.assetMode == 42 ) {
					style = "image=/graph/images/picture/router.png;spacingBottom=15px";
				}
			}
			if (currentPage.category == "cabinet_topo") {
				if(graph.getModel().cells["room"]!=null){
					style = "image=/graph/images/roomCabinet.jpg;verticalLabelPosition=center";
				}
			}
		}

		if (isPort != null && isPort == 1) {
			//如果parent不为空说明是端口
			var portParent = graph.getModel().cells[vertex.nodeParent];
			relative = true;
			value = "";
			if (vertex.offsetX != null && vertex.offsetY != null) {
				offset = new mxPoint(vertex.offsetX, vertex.offsetY);
			}

			connectable = false;
			graph.createVertexAsset(portParent, id, value, x, y, width, height, style, relative, assetId, assetType, connectable, offset, isPort, portIndex);

		} else {
			if (vertex.nodeParent != null && vertex.nodeParent != parent.id) {
				graph.createVertexAsset(cells[vertex.nodeParent], id, value, x, y, width, height, style, relative, assetId, assetType, connectable, offset, isPort, portIndex);
			} else {
				graph.createVertexAsset(parent, id, value, x, y, width, height, style, relative, assetId, assetType, connectable, offset, isPort, portIndex);

			}

		}



	}
}
function showAssetEdge(edges, editor, graph, parent){
	var cells = editor.graph.getModel().cells;
	var points = new Object();
	edges.forEach((elm,index) => {
		for(var key in cells){
		  if(elm.assetId == cells[key].assetId){
			var source = graph.getModel().cells[key]
		  }
		  if(elm.atAssetId == cells[key].assetId){
			var target = graph.getModel().cells[key]
		  }
		}

		graph.insertEdge(parent, null, '', source, target,"strokeWidth=3;strokeColor='blue'");
	})




}
function showEdge(edges, ps, editor, graph, parent) {
	var cells = editor.graph.getModel().cells;
	var points = new Object();
	for (var i = 0; i < ps.length; i++) {
		var point = ps[i];
		if (points[point.edgeId] == null) {
			points[point.edgeId] = [];
		}
		;
		points[point.edgeId].push(new mxPoint(point.pointX, point.pointY));
	}
	for (var i = 0; i < edges.length; i++) {
		var edge = edges[i];
		edge.value = edge.portMode;
		//(parent, id, value, source, target, style, points, assetId, assetType)
		var id = edge.edgeId;
		var source = graph.getModel().cells[edge.edgeSource];
		var target = graph.getModel().cells[edge.edgeTarget];
		//有些资产会被删除，有些连线的端口需要过滤以下
		if(source==null||target==null){
			if(source!=null){
				graph.model.remove(source);
			}
	        if(target!=null){
	        	graph.model.remove(target);
			}
	        continue;
		}
		var style = edge.edgeStyle;
		var position = null;
		if (edge.sourcePosition != null && edge.sourcePosition == 1) {
			position = true;
		} else {
			position = false;
		}
		if (edge.edgeParent != null) {
			graph.createEdgeAsset(cells[edge.edgeParent], id, edge.portMode, source, target, style, points[id], null, null, position);

		} else {
			graph.createEdgeAsset(parent, id, edge.portMode, source, target, style, points[id], null, null, position);

		}
		editor.fireEvent(new mxEventObject('cellsInserted', 'cells', [graph.getModel().cells[id]]));
	}


}

//获取节点信息
function createNode(cell, category, orgId) {
	//parent, id, value, x, y, width, height, style, assetId, assetType, connectable
	var node = new Object();
	node.nodeId = cell.id;
	node.name = cell.value;
	node.nodeX = cell.geometry.x;
	node.nodeY = cell.geometry.y;
	node.nodeWidth = cell.geometry.width;
	node.nodeHeight = cell.geometry.height;
	node.nodeStyle = cell.style;
	node.assetId = cell.assetId;
	node.assetType = cell.assetType;
	node.portIndex = cell.portIndex;
	node.isPort = cell.isPort;
	if (node.isPort != null && node.isPort == 1) {
		if (cell.geometry.offset != null) {
			node.offsetX = cell.geometry.offset.x;
			node.offsetY = cell.geometry.offset.y;
		}
		node.nodeParent = cell.parent.id;
		node.assetId = cell.parent.assetId;//将port的assetID变为自己的assetID
	} else {
		node.nodeParent = cell.parent.id;
	}
	node.connectable = cell.connectable == true ? 1 : 0;
	if (category == "net_topo" && node.isPort == null) {
		node.nodeConnectable = 1;
	} else {
		node.nodeConnectable = 0;
	}
	node.nodeType = category;
	node.orgId = orgId;
	return node;
}

//获取连线信息
function createEdge(cell, category, orgId,pageId) {
	//(parent, id, value, source, target, style, points, assetId, assetType)
	var edge = new Object();
	edge.edgeId = cell.id;
	edge.edgeStyle = cell.style;
	edge.edgeSource = cell.source.id;
	edge.value = cell.value
	edge.sourcePosition = cell.source.position == true ? 1 : 0;
	edge.edgeTarget = cell.target != null ? cell.target.id : null;
	edge.edgeType = category;
	edge.edgeParent = cell.parent != null ? cell.parent.id : null;
	edge.orgId = orgId;
	edge.assetId=pageId;
	return edge;
}

//获取连线信息
function createPoints(ps, points, edgeId, orgId, categroy,pageId) {
	for (var i = 0; i < points.length; i++) {
		var point = new Object();
		point.edgeId = edgeId;
		point.pointX = points[i].x;
		point.pointY = points[i].y;
		point.orgId = orgId;
		point.edgeType = categroy;
		point.assetId=pageId;
		ps.push(point);//添加折线点
	}
	return ps;
}
