/**
 * Copyright (c) 2006-2015, JGraph Ltd
 * Copyright (c) 2006-2015, Gaudenz Alder
 */
/**
 * Class: mxEventSource
 *
 * Base class for objects that dispatch named events. To create a subclass that
 * inherits from mxEventSource, the following code is used.
 *
 * (code)
 * function MyClass() { };
 *
 * MyClass.prototype = new mxEventSource();
 * MyClass.prototype.constructor = MyClass;
 * (end)
 *
 * Known Subclasses:
 *
 * <mxGraphModel>, <mxGraph>, <mxGraphView>, <mxEditor>, <mxCellOverlay>,
 * <mxToolbar>, <mxWindow>
 *
 * Constructor: mxEventSource
 *
 * Constructs a new event source.
 */
function mxEventSource(eventSource) {
    this.setEventSource(eventSource);
};

/**
 * Variable: eventListeners
 *
 * Holds the event names and associated listeners in an array. The array
 * contains the event name followed by the respective listener for each
 * registered listener.
 */
// 在数组中保存事件名称和关联的监听器。该数组包含事件名称，其后跟着每个已注册监听器的相应监听器。
mxEventSource.prototype.eventListeners = null;

/**
 * Variable: eventsEnabled
 *
 * Specifies if events can be fired. Default is true.
 */
// 指定是否可以触发事件。默认为true。
mxEventSource.prototype.eventsEnabled = true;

/**
 * Variable: eventSource
 *
 * Optional source for events. Default is null.
 */
// 事件的可选来源。默认值为null。
mxEventSource.prototype.eventSource = null;

/**
 * Function: isEventsEnabled
 *
 * Returns <eventsEnabled>.
 */
mxEventSource.prototype.isEventsEnabled = function () {
    return this.eventsEnabled;
};

/**
 * Function: setEventsEnabled
 *
 * Sets <eventsEnabled>.
 */
mxEventSource.prototype.setEventsEnabled = function (value) {
    this.eventsEnabled = value;
};

/**
 * Function: getEventSource
 *
 * Returns <eventSource>.
 */
mxEventSource.prototype.getEventSource = function () {
    return this.eventSource;
};

/**
 * Function: setEventSource
 *
 * Sets <eventSource>.
 */
mxEventSource.prototype.setEventSource = function (value) {
    this.eventSource = value;
};

/**
 * Function: addListener
 *
 * Binds the specified function to the given event name. If no event name
 * is given, then the listener is registered for all events.
 *
 * The parameters of the listener are the sender and an <mxEventObject>.
 */
/**
 * 将指定的函数绑定到给定的事件名称。如果没有给出事件名称，则为所有事件注册监听器。
 * 侦听器的参数是发送方和<mxEventObject>。
 */
mxEventSource.prototype.addListener = function (name, funct) {
    if (this.eventListeners == null) {
        this.eventListeners = [];
    }

    this.eventListeners.push(name);
    this.eventListeners.push(funct);
};

/**
 * Function: removeListener
 *
 * Removes all occurrences of the given listener from <eventListeners>.
 */
/**
 * 从<eventListeners>中删除所有出现的给定监听器。
 */
mxEventSource.prototype.removeListener = function (funct) {
    if (this.eventListeners != null) {
        var i = 0;

        while (i < this.eventListeners.length) {
            if (this.eventListeners[i + 1] == funct) {
                this.eventListeners.splice(i, 2);
            } else {
                i += 2;
            }
        }
    }
};

/**
 * Function: fireEvent
 *
 * Dispatches the given event to the listeners which are registered for
 * the event. The sender argument is optional. The current execution scope
 * ("this") is used for the listener invocation (see <mxUtils.bind>).
 *
 * Example:
 *
 * (code)
 * fireEvent(new mxEventObject("eventName", key1, val1, .., keyN, valN))
 * (end)
 *
 * Parameters:
 *
 * evt - <mxEventObject> that represents the event.
 * sender - Optional sender to be passed to the listener. Default value is
 * the return value of <getEventSource>.
 */
/**
 * 将给定事件分派给为事件注册的监听器。sender参数是可选的。
 * 当前执行范围（“this”）用于监听器调用（参阅<mxUtils.bind>）。
 *
 * evt - 表示事件的<mxEventObject>
 * sender - 要传递给监听器的可选sender。默认值是<getEventSource>的返回值
 */
mxEventSource.prototype.fireEvent = function (evt, sender) {
    if (this.eventListeners != null && this.isEventsEnabled()) {
        if (evt == null) {
            evt = new mxEventObject();
        }

        if (sender == null) {
            sender = this.getEventSource();
        }

        if (sender == null) {
            sender = this;
        }

        var args = [sender, evt];

        for (var i = 0; i < this.eventListeners.length; i += 2) {
            var listen = this.eventListeners[i];

            if (listen == null || listen == evt.getName()) {
                // if (listen == 'mouseDown') {
                // 	debugger;
                // }
                // if (listen == 'change') {
                // 	debugger;
                // }
                // console.log(listen);
                // if (listen = 'styleChanged') {
                // 	debugger;
                // }
                // if (listen = 'mouseup') {
                // 	debugger;
                // }

                if (listen != null && listen == "change") {
                    this.myEventName = "change";
                    //	debugger;
                }

                this.eventListeners[i + 1].apply(this, args);
            }
        }
    }
};