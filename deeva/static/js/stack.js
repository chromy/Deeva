function main(all_variables){
  d3.selectAll("#global_area").remove();
  d3.selectAll("#heap").remove();
  d3.selectAll("#stack").remove();

 var primitive_list = ["int", "char", "boolean", "byte", "float", "double", "long", "short"];

   var stack_variables = all_variables.stack;
  // var heap_objects = all_variables.heap;
   var heap_objects = [1,2,3];
       

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
        
     var heapRows = heap.selectAll("table")
                        .append("table")
                        .data(heap_objects)
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

   }
/*
   var heapHeader = heap.append("div")
                        .attr("id", "heapHeader")
                        .text("Objects");   
   
   var heapRows = heap.selectAll("table")
                      .append("table")
                      .data([1])
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
*/
  // on heapRowObjects will all the JsPlumb be added !!!
  var heapRowObject = d3.select("#heap")
                        .selectAll(".heapRow")
                        .selectAll(".toplevelHeapObject")
                        .append("div");

  heapRowObject.attr("class", "heapObject")
               .attr("id", function(d,i){
                        return "heap_object_" + i; 
               });
           
  var heapRowObjectElemsSelect = d3.selectAll(".heapObject");
 
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

  /*
  if ($("#stackFrameValue_j") && $("#stackFrameValue_s")) {
  //TODO: Testing Plumber will need to be delete
  //var one = jsPlumb.addEndpoint("stackFrameValue_j");
  //var two = jsPlumb.addEndpoint("stackFrameValue_s");
  var one = $("#stackFrameValue_j");
  var two = $("#stackFrameValue_s");
  jsPlumb.connect({scource:one,target:two,connector:[ "Bezier", { curviness:100 }]});
  }*/

  function create_arrows(selection){
    selection.append("div")
             .attr("class", "_jsPlumb_endpoint")
             .attr("style", "position: absolute")
             .style("position", "absolute")
             .style("height", "6px")
             .style("width", "6px");
 
   selection.append("svg")
            .attr("id", "arrow"); 

   var endpoints = selection.selectAll("._jsPlumb_endpoint");
   endpoints.append("svg")
            .style({position: "absolute", width: "6", height: "6" })
            .attr("pointer-events", "none")
            .attr("version", "1.1")
            .attr("class", "_jsPlumb_connector")
            .attr("xmlns", "http://www.w3.org/1999/xhtml");

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

  function connect_to_heap(selection){
     var new_div = selection.append("div");
         new_div.text("aaaaaaaaaaaa");
 } }
