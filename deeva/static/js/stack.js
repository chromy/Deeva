
function main(all_variables){
  d3.selectAll("#global_area").remove();
  d3.selectAll("#heap").remove();
  d3.selectAll("#stack").remove();

  // Primitive types in Java.
  var primitive_list = ["int", "char", "boolean", "byte", "float", "double", "long", "short"];

  var stack_variables = all_variables.stack;
  var heap_objects = [{type: 'T', object_type: 'Object', unique_id: '786' }, {type: 'T', object_type: 'Object', unique_id: '686' },  {type: 'T', object_type: 'Array', unique_id: '71', array: [11,12,13,14]}, {type:'T', string:'aha', unique_id:'486', object_type: 'String'}];

  var arrays = filter_heap(heap_objects, 'Array');
  var strings = filter_heap(heap_objects, 'String');
  var objects = filter_heap(heap_objects, 'Object');

  var arrows_id = [67];

  var stack_td = d3.select("#stack_td");

  var heap_td  = d3.select("#heap_td");

//   append_global_stack();

//   append_other_stacks();

   append_heap(heap_td);



   var global_area = stack_td.append("div").attr("id", "global_area");
   var stack = stack_td.append("div").attr("id", "stack");

  // var heap = heap_td.append("div").attr("id", "heap");

   var stackHeader = global_area.append("div")
                                .attr("id", "stackHeader")
                                .text("Stacks");

   // Global stack
   var stackFrame = global_area.append("div")
                              .attr("class", "stackFrame")
                              .attr("id", "globals");

   var stackFrameHeader = stackFrame.append("div")
                                    .attr("class", "stackFrameHeader")
                                    .attr("id", "globals_header")
                                    .text("Main");

   var stackFrameTable = stackFrame.append("table")
                                   .attr("class", "stackFrameVarTable")
                                   .attr("id", "global_table");
   var stackFrameSel = d3.select(".stackFrameVarTable");

   if(stack_variables != null){
   var globalVariables = stackFrameSel.selectAll("tr")
                                      .data(stack_variables)
                                      .enter()
                                      .append("tr");

   globalVariables.attr("class", "variableTr")
                  .attr("id", function(d,i){
                           return "global_"+d.name+"_tr";
                  });

   var variableTr = stackFrameSel.selectAll("tr");

   variableTr.append("td")
             .attr("class", "stackFrameVar")
             .text(function(d){
                    return d.name;
              });

   variableTr.append("td")
             .attr("class", "stackFrameValue")
             .attr("id", function(d) {
                    //TODO: must have different
                    if(d.unique_id)
                      return "stackFrameValue_heap_" + d.unique_id;
                    else
                      return "stackFrameValue_" + d.name;
             });
   var stackFrameValues = variableTr.selectAll(".stackFrameValue");
   populate_values(stackFrameValues);

   var stackFrames = stack.selectAll("div")
                          .append("div")
                          .data([1])
                          .enter();

   stackFrames.append("div")
              .attr("class", "stackFrame")
              .attr("id", function(d,i){
                            return "stack" + i;
              });

   }



  // Creates the heap and the objects in it.
   function append_heap(heap_selection){
     var heap = heap_selection.append("div").attr("id", "heap");
     var heapHeader = heap.append("div")
                          .attr("id", "heapHeader")
                          .text("Heap");

     // create a table row for each object
      var heapRows = heap.selectAll("table")
                        .data(heap_objects)
                        .enter()
                        .append("table")
                        .attr("class", "heapRow");

      var heapRow = heapRows.append("td")
                           .attr("class", "toplevelHeapObject")
                           .attr("id", function(d,i){
                              return "toplevel_heap_object_" + i;
                           });

      // on heapRowObjects will all the JsPlumb be added !!!
      var heapRowObject = heapRow.append("div")
                             .attr("class", "heapObject")
                             .attr("id", function(d,i){
                                 return "heap_object_" + d.unique_id;
                             });

      var heapObjectType = heapRowObject.append("div")
                                   .attr("class", "typeLabel")
                                   .text(function(d,i){
                                       return d.object_type;
                                   });

      var objectArray = heapRowObject.append("table")
                                 .attr("class", function(d,i){
                                       return d.object_type;
                                 });

      var objectArrayTable = objectArray.append("tbody");
      var values = objectArrayTable.append("tr").attr("id", "value");
      var values_entries = values.selectAll("td")
                               .data(function(d){
                                   if(is_of_type(d, 'Array'))
                                      return d.array;
                                   if(is_of_type(d, 'String'))
                                      return d.string;
                                   if(is_of_type(d, 'Object'))
                                      return [];
                               })
                               .enter()
                               .append("td")
                               .text(function(d,i){
                                  return d;
                               });

      var indices = objectArrayTable.append("tr").attr("id", "indice");
      var indices_entries = indices.selectAll("td")
                               .data(function(d){
                                   if(is_of_type(d, 'Array'))
                                      return d.array;
                                   if(is_of_type(d, 'String'))
                                      return d.string;
                                   if(is_of_type(d,'Object'))
                                      return [];
                               })
                               .enter()
                               .append("td")
                               .text(function(d,i){
                                   return i;
                               });
      var objects = heap_td.selectAll(".Object").selectAll("#value");
      console.log("bb" + objects);
      objects.append("span")
             .attr("class", "glyhicon glyphicon-plus");
}


  function create_arrows(selection){
   selection.append("svg")
            .attr("id", "arrow");

   //var endpoints = selection.selectAll("._jsPlumb_endpoint");
   //var svg_arrow = selection.selectAll("#arrow");
   jsPlumb.importDefaults({ 
    ConnectionsDetachable:false,
   }); 

   jsPlumb.bind("ready", function(){
      jsPlumb.Defaults.Container = "heap_stack";
      for(var i=0; i<arrows_id.length;i++){
        var source = jsPlumb.addEndpoint("stackFrameValue_heap_71", 
                           {anchor: [0.5,0.5,1, -1, 0, 2], 
                            connectionsDetachable:false,
                            cssClass: "stackPoint" 
                           });
        var target = jsPlumb.addEndpoint("heap_object_71", 
                           {anchor: "Left", 
                            endpoint: "Blank",
                            connectionsDetachable:false  
                           });
        jsPlumb.connect({source: source, 
                         target: target,
                         overlays: [["Arrow", {width:10,length:20,location:1}]],
                         Connector : ["State Machine", {proximityLimit:10}],
                         cssClass: "connectLine"
                        });
      }
   });
  }

  /* Populates the stack with the values(the actual value for primitive types
     and with arrows for objects). */ 
  function populate_values(selection){
      var primitives = selection.filter(function(d){
         return primitive_list.indexOf(d.type) >= 0;
      });

      var objects = selection.filter(function(d){
         return primitive_list.indexOf(d.type) < 0;
      });

      primitives.append("span")
                .attr("class","primitives")
                .text(function(d){
                    return d.value;
                });
      create_arrows(objects);
  }
}

/* Utility functions */
 // Returns a set of all the objects of type 'type'.
 function filter_heap(heap_objects, type){
   heap_objects.filter(function(d){
      return d.object_type == type;
    });
 }

 // Returns true if the object is of type 'type', false otherwise.
 function is_of_type(heap_element, type){
   return heap_element.object_type == type;
 }
