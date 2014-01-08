
function main(all_variables){
  d3.selectAll("#global_area").remove();
  d3.selectAll("#heap").remove();
  d3.selectAll("#stack").remove();

  // Primitive types in Java.
  var primitive_list = ["int", "char", "boolean", "byte", "float", "double", "long", "short"];

  var stack_variables = all_variables.stack || [];
  //var unique_id_list = filter_stack(stack_variables);
  var unique_id_list = {unique_id: 71, type: 'java.lang.String[]'};
  if(unique_id_list != undefined){
  $.post("getHeapObject", unique_id_list).done(function(data){
     console.log("data---", data);
     console.log("data", [data].length);
     var heap_td  = d3.select("#heap_td");
     append_heap(heap_td, [data.data]);
  });
 }

//[{type: 'T', object_type: 'Object', unique_id: '701' }, {type: 'T', object_type: 'Object', unique_id: '686' },  {type: 'T', object_type: 'Array', unique_id: '71', array: [11,12,13,14]}, {type:'T', string:'aha', unique_id:'486', object_type: 'String'}];

  //var arrays = filter_heap(heap_objects, 'Array');
  //var strings = filter_heap(heap_objects, 'String');
  //var objects = filter_heap(heap_objects, 'Object');

  var stack_td = d3.select("#stack_td");

  var stack = stack_td.append("div").attr("id", "stack");

  var stackHeader = stack.append("div")
                         .attr("id", "stackHeader")
                         .text("Stacks");
  var stackFrames = stack.append("div")
                         .attr("id", "stackFrames");

  append_stacks(stackFrames, [stack_variables, stack_variables, stack_variables], primitive_list);

 }


function append_stacks(stack_selection, stack_variables, primitive_list){
   
   // create a div for each stack
   var stackFrames = stack_selection.selectAll("div")
                                    .data(stack_variables)
                                    .enter()
                                    .append("div");
   stackFrames.attr("class", "stackFrame")
              .attr("id", function(d,i){
                            return "stack" + i;
              });

   //name of the stack
   var stackFrameHeaders = stackFrames.append("div")
                                    .attr("class", "stackFrameHeader")
                                    .attr("id", function(d, i){
                                        return "stackHeader" + i;
                                    })
                                    .text("Main");

   //variable table
   var stackFrameTable = stackFrames.append("table")
                                   .attr("class", "stackFrameVarTable")
                                   .attr("id",function(d, i){
                                        return "stackVarTable" + i;
                                   });

   var stackVariables = stackFrameTable.selectAll("tr")
                                       .data(function(d, i){
                                          return d;
                                       })
                                       .enter()
                                       .append("tr");

   stackVariables.attr("class", "variableTr")
                  .attr("id", function(d,i){
                           return "stack_" + d.name + "_tr";
                  });

   stackVariables.append("td")
                 .attr("class", "stackFrameVar")
                 .text(function(d){
                    return d.name;
                 });

   var stackFrameValues = stackVariables.append("td")
                 .attr("class", "stackFrameValue")
                 .attr("id", function(d) {
                    if(d.unique_id)
                      return "stackFrameValue_heap_" + d.unique_id;
                    else
                      return "stackFrameValue_" + d.name;
                 });

   populate_values(stackFrameValues, primitive_list);
}

/* Populates the stack with the values(the actual value for primitive types
   and with arrows for objects). */
function populate_values(selection, primitive_list){
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
}

// Creates the heap and the objects in it.
function append_heap(heap_selection, heap_objects){
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
                                        if(is_of_type(d, 'object'))
                                           return d.type;
                                        else
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
                                 if(is_of_type(d, 'array')){
                                   console.log("sasas" + d.array);
                                   return d.array;}
                                 if(is_of_type(d, 'string'))
                                   return d.string;
                                 if(is_of_type(d, 'object'))
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
                                   if(is_of_type(d, 'array'))
                                      return d.array;
                                   if(is_of_type(d, 'string'))
                                      return d.string;
                                   if(is_of_type(d,'object'))
                                      return [];
                               })
                               .enter()
                               .append("td")
                               .text(function(d,i){
                                   return i;
                               });
   var objects = heap_selection.selectAll(".object").selectAll("#value");

   var objects_button = objects.append("button")
                               .attr("type", "button")
                               .attr("class", "btn btn-default");
   objects_button.append("span")
                 .attr("class", "glyhicon glyphicon-plus");
  //create_arrows(objects);
}


  function create_arrows(selection){
   selection.append("svg").attr("id", "arrow");

   // makes connectors undraggable
   jsPlumb.importDefaults({
    ConnectionsDetachable:false,
   });

   jsPlumb.bind("ready", function(){
      jsPlumb.Defaults.Container = "heap_stack";
        var source = jsPlumb.addEndpoint("stackFrameValue_heap_71",
                           {anchor: [0.5, 0.5, 0, -1, 0, 2],
                            connectionsDetachable:false,
                            cssClass: "stackPoint"
                           });
        var target = jsPlumb.addEndpoint("heap_object_71",
                           {anchor: "Left",
                            endpoint: "Blank",
                            connectionsDetachable:false
                           });
/*
        jsPlumb.connect({source: source,
                         target: target,
                         overlays: [["Arrow", {width: 6,length: 6,location:1}]],
                         Connector : ["State Machine", {proximityLimit:1}],
                         cssClass: "connectLine"
                        });
*/
   });
  }


/* Utility functions */


 function filter_stack(stack_variables){
   if(stack_variables){
     stack_variables.filter(function(d){
       return d.unique_id != undefined;

     });
   }
   else
     return [];
 }

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
