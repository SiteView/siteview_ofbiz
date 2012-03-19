-module (web_remotent).
-include ("wf.inc").
-include("monitor.hrl").

-compile(export_all).

main() ->
    wf:session("mulMachine", undefined),
	#template { file=web_page_common:get_template_file("userpreferences.html"), bindings=[
	{'Group', remotent},
	{'Item', samples}
]}.

title() -> "Remote Windows Servers".
%headline() -> "User Login".


get_data() ->
    LabelList = [],
	NTmachineList = api_machine:getMachineByOs("nt", 0, 0, "", ""),
    LabelKvs = build_label_kvs(LabelList),
	%sort NTmachineList ...
	[get_content(M, LabelKvs) || M <- NTmachineList].
	
get_content(M, LabelKvs) ->
	%Id = atom_to_list(M#machine.id),
	Id = M#machine.id,
    Label = get_label_name(M),
    SelectLabel =[#inplace_button{id=newLabel,text=?TXT("Select Label"),width=250,height=60,body=[     
                        #span {class="label", text=?TXT("Label Name")++":"},#dropdown {id=list_to_atom("dpl"++Id),options=tr069_utils:print_lists(LabelKvs,[""])}
                        ],postback={selectlabel,"dpl"++Id, Id}}],  
	[{select,M},M#machine.name, "/web/updateremotent?machine="++Id, M#machine.host, M#machine.status, M#machine.method, "/web/testmachine?machine="++Id++"&page=remotent", "/web/deletemachine?page=nt&machine="++Id].

get_map() -> [nodeselect@postback,nameLabel@text, nameLabel@url,serverLable@text, statusLable@text, methodLable@text, testLink@url, delLink@url].


%% 将标签列表构造为[{K,V}|T]
build_label_kvs([]) ->
    [];
build_label_kvs([Label=#machine_label{}|T]) ->
    [{Label#machine_label.name, Label#machine_label.name}] ++
    build_label_kvs(T);
build_label_kvs([H|T]) ->
    build_label_kvs(T).

%% 判断标签是否为空，空的话就为默认的标签
get_label(M) ->
    case M#machine.label of
        "" ->
            ?DefMachNTLabel;
        _ ->
            M#machine.label
    end.

%% 获取标签名
get_label_name(M) ->
    Label = get_label(M),
    textutils:get_nt_label_name(Label).

body()->		
	Map = get_map(),
	Data = get_data(),
	Body = [		
			#p{},		
			#h2{ text="Remote Windows Servers", class=userprofilestext },
			#panel{ body=?OEM(?TXT("A list of Windows/2000/2003 servers with connectivity and remote access permissions for monitoring from this SiteView instance. Remote connection options include WMI or SSH."))},	    
			#panel{ body="<b>Note:</b> Connections using SSH version 2 require additional set up."},
			#p{},
			#table { class=bodytable, rows=[
				#tablerow { cells=[
                    #tableheader { text="Select", class=remotentname },							
					#tableheader { text="Name", class=remotentname },							
					#tableheader { text="Server", class=remotentserver },
					#tableheader { text="Status", class=remotentstatus },
					#tableheader { text="Method", class=remotentmethod },
					#tableheader { text="Test", class=remotenttest },
					#tableheader { text="Del", class=remotentdel }
   %%                 #tableheader { text="Add Monitor Set",width="100px", class=remotentadd }
				   ]},
				#bind { id=tableBinding, data=Data, map=Map, body=#tablerow { cells=[
                    #tablecell { id=nodecell,body=[#checkbox{id=nodeselect, html_encode=false,checked=false}] },							
					#tablecell { body=#link {id=nameLabel, class=linkstyle} },							
					#tablecell { id=serverLable },
					#tablecell { id=statusLable, style="font-weight: bold;font-style: italic;color: #222;" },							
					#tablecell { id=methodLable },
					#tablecell { body=#link { id=testLink, text="Test", class=linkstyle} },
					#tablecell { body=#link { id=delLink, text="X", class=linkstyle} }
 %%                   #tablecell {body=make_dropdown()}					
				   ]}}			
				
			]},			
			#p{},
			#panel{ body=[ 
						#link { text="Add", class=addLink, url="/web/addremotent" }, 
						#label{ text="a Remote Machine" } 
						]},
            #br{},
            #br{},
			#panel{ body=[ 
                        #h2{text="Update Machines", class=userprofilestext },
                        #p{},
                        #span{text="Password: "},
                        #password{id=pwdUpdateMachine, text=""},
                        #p{},
						#button {id=btnAddTag, text="Update", postback={btn_click,btnUpdateMachine}}
						]}		
	],
	
    wf:render(Body).

make_dropdown() ->
    GroupsList = api_siteview:getAllGroups(),
    make_dropdown_t(GroupsList,length(GroupsList),[]).    
make_dropdown_t(_,0,E) -> 
   TempId = list_to_atom(textutils:guid()),
   #dropdown{id=TempId,options=[#option {value=false, text="False"}|E],postback={add,TempId}};
make_dropdown_t([{Name,Id}|B],Len,Ed) ->
    if Name == "Health" ->
        make_dropdown_t(B,Len-1,Ed);
    true -> 
        make_dropdown_t(B,Len-1,[#option {value=Id, text=Name}|Ed]) 
    end.
   
process_gropid(GropId) ->
    process_gropid_t(GropId,length(GropId),""). 
process_gropid_t(_,0,Id) ->
    wf:redirect("/web/monitorset_list?parent="++Id); 
process_gropid_t([A|B],Len,E) ->
    if A == "false" ->
        process_gropid_t(B,Len-1,E);
    true ->
        process_gropid_t(B,0,A)
    end. 

parse_to_machineList([], MulKVS) ->
    [];
parse_to_machineList([{Id, M = #machine{}}|T], MulKVS) ->
    %%Len = lists:flatlength(MulKVS),
    case lists:keysearch(passwd,1,MulKVS) of
        {value,{passwd,Password}} ->
            [M#machine{passwd=Password}];
        _ ->
            [M]
    end ++
    parse_to_machineList(T, MulKVS);
parse_to_machineList([H|T], MulKVS) ->
    parse_to_machineList(T, MulKVS).


event({selectlabel,CtlId, Id}) ->
    Tag = 
        case wf:q(CtlId) of
            [V] ->
                V;
            _ ->
                ""
        end,
    Machine = api_machine:get_machine(erlang:list_to_atom(Id)),
    case Machine of
        Ma = #machine{} ->
            case api_machine:save_tagToMachine([Ma], Tag) of
                {ok, _} ->
                    wf:redirect("/web/remotent");
                _ ->
                    wf:wire(#alert{text="select label fail"})
            end;
        _ ->
            wf:wire(#alert{text="select label fail"})
    end,
    ok;
event({tagGroup,M}) ->
    io:format("tagGroup~n"),
    Label = get_label(M),
    io:format("Label = ~p~n", [Label]),
    wf:session("tagid", Label),
    wf:redirect("/web/viewgroup_remotent"),
    ok;
event({btn_click,btnUpdateMachine}) ->
    SeReList =
    case wf:session("mulMachine") of
        ReList when erlang:is_list(ReList) ->
            ReList;
        _ ->
            []
    end,
    [PwdUpdateMachine] = wf:q(pwdUpdateMachine),
    MachUpdates = [{passwd, PwdUpdateMachine}],
    NSeReList = parse_to_machineList(SeReList, MachUpdates),
    wf:session("mulMachine", undefined),
    case api_machine:update_Machines(NSeReList) of
        {ok, _} ->
            wf:wire(#alert{text=?TXT("Update Machines Ok")});
        _ ->
            wf:wire(#alert{text=?TXT("Update Machines Failed")})
    end,
    ok;
event({select,M}) ->
    SeReList =
    case wf:session("mulMachine") of
        ReList when erlang:is_list(ReList) ->
            ReList;
        _ ->
            []
    end,
    case lists:keymember(M#machine.id, 1, SeReList) of
        true ->
            NSeReList = lists:keydelete(M#machine.id, 1, SeReList);
        _ ->
            NSeReList = lists:keystore(M#machine.id, 1, SeReList, {M#machine.id, M})
    end,
    wf:session("mulMachine", NSeReList),
    ok;
event({add,Id}) ->
    GropId = wf:q(Id),
    process_gropid(GropId);       
event(_) -> ok.