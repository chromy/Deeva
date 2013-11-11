function main(){
 var primitive_list = ["int", "char", "boolean", "byte", "float", "double", "long", "short"];
   var all_variables = {"stack": [{type:"int", name:"x", value: "3"},
                              {type:"char", name:"y", value: "z"},
                              {type:"string", name:"bob", value: "bob"},
                              {type:"int[]", name:"array", value: "[]"}
                             ],
                    "heap": []
                   }
   var variables = all_variables.stack;

   var stack_td = d3.select("#stack_td");
   console.log(stack_td);
   var heap_td  = d3.select("#heap_td");

   var global_area = stack_td.append("div").attr("id", "global_area");
   var stack = stack_td.append("div").attr("id", "stack");

   var heap = heap_td.append("div").attr("id", "heap");

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

   var globalVariables = stackFrameSel.selectAll("tr")
                                      .data(variables)
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
             .text(function(d){
                 if(primitive_list.indexOf(d.type) >= 0)
                    return d.value  
                 else{

                 }
              });

   var stackFrames = stack.selectAll("div")
                          .append("div") 
                          .data([1,2,3])
                          .enter();

   stackFrames.append("div")
              .attr("class", "stackFrame")
              .attr("id", function(d,i){
                            return "stack" + i;  
              });

   var heapHeader = heap.append("div")
                        .attr("id", "heapHeader")
                        .text("Objects");   
   
   var heapRows = heap.selectAll("table")
                      .append("table")
                      .data([1,2,3,4,5,6])
                      .enter();
 
   heapRows.append("table")
           .attr("class", "heapRow");

   var heapRow = d3.select("#heap")
                   .selectAll(".heapRow")
                   .append("td");

   heapRow.attr("class", "toplevelHeapObject")
          .attr("id", function(d,i){
                        return "toplevel_heap_object_" + i; 
          });

  // on heapRowObjects will all the JsPlumb be added !!!
  var heapRowObject = d3.select("#heap")
                        .selectAll(".heapRow")
                        .selectAll(".toplevelHeapObject")
                        .append("div");

  heapRowObject.attr("class", "heapObject")
               .attr("id", function(d,i){
                        return "heap_object_" + i; 
               });
           
  var heapRowObjectElemsSelect = 
                  d3.select("#heap")
                    .selectAll(".heapRow")
                    .selectAll(".toplevelHeapObject")
                    .selectAll(".heapObject");
 
  varheapObjectType = heapRowObjectElemsSelect.append("div")
                                              .attr("class", "typeLabel")
                                              .text("label");
 
  var objectArray = heapRowObjectElemsSelect.append("table")
                                            .attr("class", "array");
  
  var objectArraySelection = heapRowObjectElemsSelect.selectAll("table")
                                                     .append("tbody");

  var objectElementsSelection = heapRowObjectElemsSelect.selectAll("table")
                                                        .select("tbody");

  var objectEntries = objectElementsSelection.append("tr").attr("id", "value");
  var objectEntriesIndice = objectElementsSelection.append("tr")
                                                   .attr("id", "indice");
  
  var objectEntriesSel = objectElementsSelection.select(".value");
  var objectEntriesIndiceSel = objectElementsSelection.select(".indice");
  
}
