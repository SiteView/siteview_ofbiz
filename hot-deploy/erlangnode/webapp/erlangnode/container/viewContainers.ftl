<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->


<div id="stats-bins-history" class="screenlet">
  <div class="screenlet-title-bar">
    <ul>
      <li class="h3">${uiLabelMap.WebtoolsComponentsLoaded}</li>
    </ul>
    <br class="clear"/>
  </div>
  
  <#list listData?if_exists as data>
	${data} </br>
  </#list>
  <@ofbizUrl>ViewComponents</@ofbizUrl>
  <a target='_target' href='<@ofbizUrl>ViewComponents</@ofbizUrl>' class='buttontext'>Components</a>
  <a target='main' href='<@ofbizUrl>StartContainer</@ofbizUrl>' class='buttontext'>Start</a>
  <a target='main' href='<@ofbizUrl>StopContainer</@ofbizUrl>' class='buttontext'>Stop</a>
</div>
