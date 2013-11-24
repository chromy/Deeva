function main(all_variables){
  d3.selectAll("#global_area").remove();
  d3.selectAll("#heap").remove();
  d3.selectAll("#stack").remove();

 var primitive_list = ["int", "char", "boolean", "byte", "float", "double", "long", "short"];

   var stack_variables = all_variables.stack;
  // var heap_objects = all_variables.heap;
   var heap_objects = [1,2,3];
   var heap_objects_value = [[19,12,11],[],[11]];       

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
   console.log(stack_variables);
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

   function append_heap(heap_selection){
     var heap = heap_selection.append("div").attr("id", "heap");
     var heapHeader = heap.append("div")
                          .attr("id", "heapHeader")
                          .text("Objects");   

     // create a table row for each object   
     var heapRows = heap.selectAll("table")
                        .data(heap_objects_value)
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
                                 return "heap_object_" + i; 
                             });
           
  var heapObjectType = heapRowObject.append("div")
                                   .attr("class", "typeLabel")
                                   .text(function(d,i){
                                       return "label"+i;
                                   });
 
  var objectArray = heapRowObject.append("table")
                                 .attr("class", "array");

  var objectArrayTable = objectArray.append("tbody");  
  var indices = objectArrayTable.append("tr").attr("id", "indice");
  var indices_entries = indices.selectAll("td")
                               .data(function(d){
                                   return d;
                               })
                               .enter()
                               .append("td")
                               .text(function(d,i){
                                   return i;
                               });
  var values = objectArrayTable.append("tr").attr("id", "value");
  var values_entries = values.selectAll("td")
                               .data(function(d){
                                   return d;
                               })
                               .enter()
                               .append("td")
                               .text(function(d,i){
                                   return d;
                               });
  
}


  function create_arrows(selection){
   selection.append("svg")
            .attr("id", "arrow"); 

   var endpoints = selection.selectAll("._jsPlumb_endpoint");
   var svg_arrow = selection.selectAll("#arrow");
 
   jsPlumb.bind("ready", function(){

   var e0 = jsPlumb.addEndpoint("global_a_tr");
   var e1 = jsPlumb.addEndpoint("global_b_tr");
   
   jsPlumb.connect({source: e0, target: e1});
   });
  }

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
