%% ---
%% dbcs_machine
%%
%%---
-module(dbcs_machine).
-compile(export_all).
-define(Table,"machine").
-define(MachineLabel,"machineLabel").
-include("monitor.hrl").
-include("dbcs_common.hrl").
-include("remoteMachine.hrl").

get_field(Name,Data)->
	case lists:keysearch(Name,1,Data) of
		{value,{Name,Type,Val}}->
			
			{_K,V} = dbcs_base:db2term(Name,Type,Val),
			V;
		_->
			undefined
	end.

db_to_machine(Data)->
    Mac =
	case Data of
		{_,_,Id,_,_,_,_,_,_,_,_,_,_,_,Adv}->
			#machine{
			id=Id,
			name=get_field(name,Adv),
			host=get_field(host,Adv),
			login=get_field(login,Adv),
			passwd=get_field(passwd,Adv),
			os=get_field(os,Adv),
			trace = get_field(trace,Adv),
			status=get_field(status,Adv),
			method=get_field(method,Adv),
			prompt = get_field(prompt,Adv),
			loginprom = get_field(loginprom,Adv),
			passwdprom = get_field(passwdprom,Adv),
			secondprom = get_field(secondprom,Adv),
			secondresp = get_field(secondresp,Adv),
			initshell = get_field(initshell,Adv),
			sshcommand=get_field(sshcommand,Adv),
			sshclient=get_field(sshclient,Adv),
			sshport=get_field(sshport,Adv),
			version=get_field(version,Adv),
			keyfile=get_field(keyfile,Adv),
			sshauthmethod=get_field(sshauthmethod,Adv),
			remoteencoding = get_field(remoteencoding,Adv),
			disableconncaching = get_field(disableconncaching,Adv),
			connlimit = get_field(connlimit,Adv),
            type = get_field(type , Adv),
            other=get_field(other,Adv),
            pwdmode=get_field(pwdmode,Adv)
			};
		_->
			#machine{}
	end,
    NMach = structMacByPwdMode(Mac).
    


get_machine_match(Where)->
	erlcache:start_link(),
	App = dbcs_base:get_app(),
	Key = erlang:phash2({App,machine,Where}),
	case erlcache:get(Key) of
		{ok,R}->
			R;
		_->
			try
			Ret = db_ecc:get_ofbizdata(?DBName,?Table,Where),
			%Mchs = [db_to_machine(X)||X <- Ret],
			Mchs = [ofbizdb_to_machine(X)||X <- Ret],
			erlcache:set(Key,Mchs,10),
			Mchs
			catch
			_:_->pass,%io:format("ERROR:dbcs_machine:get_machine_match catch a exception!"),
			[]
			end
	end.
    
get_machine_match(Host,Where)->
	try
	Ret = dbcs_tr069:get_data(Host,?DBName,?Table,Where),
	%io:format("dbcs machine Ret:~p~n",[Ret]),
    [db_to_machine(X)||X <- Ret]
	catch
	_:_->pass,%io:format("ERROR:dbcs_machine:get_machine_match catch a exception!"),
    []
	end.

get_machine_by_host(Localhost,Host)when is_list(Host)->
	get_machine_match(Localhost,"my.host="++Host);
get_machine_by_host(Localhost,Host)when is_atom(Host)->
	get_machine_match(Localhost,"my.host="++atom_to_list(Host)).


get_machine(Name)when is_list(Name)->
	get_machine_match("my.name="++Name);
get_machine(Name)when is_atom(Name)->
	get_machine_match("my.name="++atom_to_list(Name)).

get_machine_by_host(Host)when is_list(Host)->
	get_machine_match("my.host="++Host);
get_machine_by_host(Host)when is_atom(Host)->
	get_machine_match("my.host="++atom_to_list(Host)).

remove_machine(Id)->
	db_ecc:delete_data(?DBName,?Table,"id="++atom_to_list(Id)).

remove_machine_by_host(Host)->
	case get_machine_match("my.host=" ++ Host) of
		[]->
			{error,not_found};
		R->
			Ret = [remove_machine(X#machine.id) || X<-R],
			case lists:keymember(error,1,Ret) of
				true->
					{error,Ret};
				_->
					{ok,removed}
			end
	end.


%% @doc 新建一个machine
create_machine(Mach)->
	Id = Mach#machine.id,
    %%NMach = structMacByPwdMode(Mach),
	Adv = record_to_db(Mach),
	%%cxy 2011/07/27
	%%db_ecc:insert_data(?DBName, ?Table, {content, list_to_atom(?Table), Id, <<"machine">>,null,null,null,null,?Author,null,null,null,null,null,Adv}).
	%db_ecc:insert_data(?DBName, ?Table, {content, list_to_atom(?Table), Id, %"machine",null,null,null,null,?Author,null,null,null,null,null,record_to_ofbizdb(Mach)}).
	db_ecc:insert_data(?DBName, ?Table,record_to_ofbizdb(Mach)).


%% @doc 新建一个machine
update_machine(Mach) ->
	Id = Mach#machine.id,
	Where = "id = " ++ atom_to_list(Id),	
    %%NMach = structMacByPwdMode(Mach),
	Adv = record_to_db(Mach),
	%%cxy 2011/07/27
	%%NewRecord = {content, list_to_atom(?Table), Id, <<"machine">>, null, null, null, null, ?Author, null, null, null, null, null, Adv},
	NewRecord = {content, list_to_atom(?Table), Id, "machine", null, null, null, null, ?Author, null, null, null, null, null, record_to_ofbizdb(Mach)},
%	db_ecc:update_data(?DBName, ?Table, Where, NewRecord).
	db_ecc:insert_data(?DBName, ?Table,record_to_ofbizdb(Mach)).
	
	
%% @doc 获得所有的machine信息
get_all()->
	Ret = db_ecc:get_ofbizdata(?DBName, ?Table, ""),
%	ofbizdb_to_machine(Ret).
	case is_list(Ret) of
		false ->
			[];
		true ->
%%			[db_to_machine(Id, Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret]
			[ofbizdb_to_machine(X) || X <- Ret]
	end.


%% @doc 获得特定的machine信息
get_machineById(Id)->
	Ret = db_ecc:get_ofbizdata(?DBName, ?Table, "id="++Id),
	case is_list(Ret) of
		false ->
			[];
		true ->
%%			[db_to_machine(Id, Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machine(X) || X <- Ret]
	end.

%	ofbizdb_to_machine(Ret).
%	case Ret of		
%		[{content, _, MId, _, _, _, _, _, _, _, _, _, _, _, Advance}] ->
%			db_to_machine(MId, Advance);
 %		_ ->
%			#machine{}
%	end.
	
	
%% @doc 获得所有nt machine信息
get_NTmachine()->
	Ret = db_ecc:get_ofbizdata(?DBName, ?Table, "my.os=nt"),
%	ofbizdb_to_machine(Ret).
	case is_list(Ret) of
		false ->
			[];
		true ->
%%			[db_to_machine(Id, Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machine(X) || X <- Ret]
	end.
	
	
%% @doc 获得所有unix machine信息
get_Unixmachine()->
	Ret = db_ecc:get_ofbizdata(?DBName, ?Table, "my.os!=nt"),
%	ofbizdb_to_machine(Ret).
	case is_list(Ret) of
		false ->
			[];
		true ->
%%			[db_to_machine(Id, Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machine(X) || X <- Ret]
	end.

	
%%查询Machine Name
get_machinename(Id) ->
	Ret = db_ecc:get_ofbizdata(?DBName, ?Table, "id="++Id),
	case Ret of
		[] ->
			not_existed;
%		[{content, _, _, _, _, _, _, _, _, _, _, _, _, _, Ad}] ->
		[Ad] ->
			N = case lists:keysearch(name, 1, Ad) of		
					{value, {name, string, _BinName}} ->
						try binary_to_list(_BinName)
						catch
							_ : _ -> ""
						end;		
					false ->
						""
				end,
			{ok, N};			
		_ ->
			error
	end.


%%删除一个machine
delete(Id)->
	db_ecc:delete_data(?DBName, ?Table, "id="++Id).
	
%% @doc 获取所有未分组的unix类型的标签标签,
get_Unix_Machine_ungroup() ->
    %%++"&my.label=''"
    Ret = db_ecc:get_ofbizdata(?DBName, ?Table, "my.os!=nt"),
	case is_list(Ret) of
		false ->
			[];
		true ->
%			[db_to_machine(Id, Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machine(X) || X <- Ret]
	end.
    
%% @doc 获取所有未分组的windows类型的标签,
get_NT_Machine_ungroup() ->
    Ret = db_ecc:get_ofbizdata(?DBName, ?Table, "my.os=nt"),
	case is_list(Ret) of
		false ->
			[];
		true ->
%			[db_to_machine(Id, Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machine(X) || X <- Ret]
	end.
    
%% @doc 根据TagId获取所有未分组的unix类型的标签标签,
get_Unix_Machine_ByTag(TagId) ->
    %%++"&my.label=''"
    if 
        TagId =:= "" ; TagId =:= ?DefMachUnixLabel ->
            get_Unix_Machine_Undefined();
        true ->
            Ret = db_ecc:get_ofbizdata(?DBName, ?Table, "my.os!=nt"++"&my.label='"++TagId++"'"),
            case is_list(Ret) of
                false ->
                    [];
                true ->
%                    [db_to_machine(Id, Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
		     [ofbizdb_to_machine(X) || X <- Ret]
            end
    end.
    
%% @doc 根据TagId获取所有未分组的windows类型的标签,
get_NT_Machine_ByTag(TagId) ->
    if
        TagId =:= "" ; TagId =:= ?DefMachNTLabel ->
            get_NT_Machine_Undefined();
        true ->
            Ret = db_ecc:get_ofbizdata(?DBName, ?Table, "my.os=nt"++"&my.label='"++TagId++"'"),
            case is_list(Ret) of
                false ->
                    [];
                true ->
%                    [db_to_machine(Id, Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
		     [ofbizdb_to_machine(X) || X <- Ret]
            end
    end.
   
%% @doc 根据TagId获取所有设备
get_Machine_ByTag(TagId) ->
    Ret = db_ecc:get_ofbizdata(?DBName, ?Table, "my.label='"++TagId++"'"),
	case is_list(Ret) of
		false ->
			[];
		true ->
%			[db_to_machine(Id, Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machine(X) || X <- Ret]
	end.
    
%% @doc 根据TagId获取所有未定义的windows类型的标签,
get_NT_Machine_Undefined() ->
    Ret = db_ecc:get_ofbizdata(?DBName, ?Table, "my.os=nt"++"&(my.label='"++?DefMachNTLabel++"'|my.label='')"),
	case is_list(Ret) of
		false ->
			[];
		true ->
%			[db_to_machine(Id, Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machine(X) || X <- Ret]
	end.
   
%% @doc 根据TagId获取所有未定义的的unix类型的标签标签,
get_Unix_Machine_Undefined() ->
    %%++"&my.label=''"
    Ret = db_ecc:get_ofbizdata(?DBName, ?Table, "my.os!=nt"++"&(my.label='"++?DefMachUnixLabel++"'|my.label='')"),
	case is_list(Ret) of
		false ->
			[];
		true ->
%			[db_to_machine(Id, Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machine(X) || X <- Ret]
	end.

%% @doc 万能数据库操作
get_machine_where(Where, Order)->     
    Ret = db_ecc:get_ofbizdata_stat(?DBName, ?Table, Where,Order),
    %build_machine(Ret).
    [ofbizdb_to_machine(X) || X <- Ret].

%% 
build_machine({R1, R2, Count,[]}) ->
    [];
%build_machine({R1, R2, Count,[{content, _,Id, _, _, _, _, _, _, _, _, _, _, _, Advance}|T]}) ->
%    VCount = erlang:list_to_binary(erlang:integer_to_list(Count)),
%    NAdv =  [{total, number, VCount}],
%    [db_to_machine(Id, Advance++NAdv)] ++ 
%    build_machine({R1, R2, Count,T});
build_machine({R1, R2, Count,[H|T]}) ->
    VCount = erlang:list_to_binary(erlang:integer_to_list(Count)),
    NAdv =  [{total, VCount}],
    [ofbizdb_to_machine(H++NAdv)] ++ 
    build_machine({R1, R2, Count,T});
build_machine({R1, R2, Count,[H|T]}) ->
    build_machine({R1, R2, Count,T});
build_machine(Other) ->
    [].

%% 
structMacByPwdMode(Machine) ->
    %%io:format("Machine#machine.pwdmode: ~p~n", [Machine#machine.pwdmode]),
    case Machine#machine.pwdmode of
        ?PWDMODE_DEFAULT ->
            Machine;
        PwdMode when erlang:is_list(PwdMode) ->
            io:format("PwdMode: ~p~n", [PwdMode]),
            PwdModeAtom = erlang:list_to_atom(PwdMode),
            io:format("PwdModeAtom: ~p~n", [PwdModeAtom]),
            Pwd = 
            case api_preferences:get_prefs(?PWDMNG, PwdModeAtom) of
                {ok,[{_, Ret=#pwdSet{}}]}->
                    Ret#pwdSet.pwd;
                _->
                    Machine#machine.passwd
            end,
            io:format("Pwd: ~p~n", [Pwd]),
            Machine#machine{passwd=Pwd};
        _ ->
            Machine
    end.

%% ------------- label 操作------------

%% 
build_tag({R1, R2, Count,[]}) ->
    [];
build_tag({R1, R2, Count,[{content, _,Id, _, _, _, _, _, _, _, _, _, _, _, Advance}|T]}) ->
    VCount = erlang:list_to_binary(erlang:integer_to_list(Count)),
    NAdv =  [{total, number, VCount}],
    [db_to_machinelabel(Advance++NAdv)] ++
    build_tag({R1, R2, Count,T});
build_tag({R1, R2, Count,[H|T]}) ->
    build_tag({R1, R2, Count,T});
build_tag(Other) ->
    [].

%% @doc 万能数据库操作
get_tag_where(Where, Order)->     
    Ret = db_ecc:get_ofbizdata_stat(?DBName, ?MachineLabel, Where,Order),
    build_tag(Ret).

%% 获取所有标签
get_all_label() ->
    Ret = db_ecc:get_ofbizdata(?DBName, ?MachineLabel, ""),
	case is_list(Ret) of
		false ->
			[];
		true ->
%			[db_to_machinelabel(Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machinelabel(X) || X <- Ret]
	end.

%% @doc 根据标签组类型获取所有标签
get_label_type(Type) ->
    Where = "my.type="++Type,
    Ret = db_ecc:get_ofbizdata(?DBName, ?MachineLabel, Where),
	case is_list(Ret) of
		false ->
			[];
		true ->
%			[db_to_machinelabel(Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machinelabel(X) || X <- Ret]
	end.

%% @doc 获取所有unix类型的标签标签,
get_Unix_label() ->
    Where = "my.type="++?MachUnixLabel,
    Ret = db_ecc:get_ofbizdata(?DBName, ?MachineLabel, Where),
	case is_list(Ret) of
		false ->
			[];
		true ->
%			[db_to_machinelabel(Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machinelabel(X) || X <- Ret]
	end.
    
%% @doc 获取所有windows类型的标签,
get_NT_label() ->
    Where = "my.type="++?MachNTLabel,
    Ret = db_ecc:get_ofbizdata(?DBName, ?MachineLabel, Where),
	case is_list(Ret) of
		false ->
			[];
		true ->
%			[db_to_machinelabel(Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machinelabel(X) || X <- Ret]
	end.

%% @doc 获取所有系统标签,
get_syslabel_label() ->
    Where = "my.syslabel=true",
    Ret = db_ecc:get_ofbizdata(?DBName, ?MachineLabel, Where),
	case is_list(Ret) of
		false ->
			[];
		true ->
%			[db_to_machinelabel(Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machinelabel(X) || X <- Ret]
	end.

%% @doc 根据id获取标签
get_LabelById(Id) ->
    Where = "id="++Id,
    Ret = db_ecc:get_ofbizdata(?DBName, ?MachineLabel, Where),
	case is_list(Ret) of
		false ->
			[];
		true ->
%			[db_to_machinelabel(Advance) || {content, _, Id, _, _, _, _, _, _, _, _, _, _, _, Advance} <- Ret] 			
			[ofbizdb_to_machinelabel(X) || X <- Ret]
	end.
    
    
%% @doc 新建一个标签
create_label(Label)->
	Id = erlang:list_to_atom(Label#machine_label.id),
	Adv = machinelabel_to_db(Label),
	db_ecc:insert_data(?DBName, ?MachineLabel, {content, list_to_atom(?MachineLabel), Id, <<"machineLabel">>,null,null,null,null,?Author,null,null,null,null,null,Adv}).
    
%% @doc 新建一个标签
update_label(Label) ->
    Id = Label#machine_label.id,
	Where = "id = " ++ Id,
	Adv = machinelabel_to_db(Label),
	NewRecord = {content, list_to_atom(?MachineLabel), Id, <<"machineLabel">>, null, null, null, null, ?Author, null, null, null, null, null, Adv},
  db_ecc:update_data(?DBName, ?MachineLabel, Where, NewRecord).
    
%% @doc 根据id删除标签一个标签
remove_label(Id)->
	db_ecc:delete_data(?DBName,?MachineLabel,"id='"++Id++"'").
    

%% @doc 删除所有标签
remove_AllLabel() ->
    Maches = get_all_label(),
    remove_AllLabel_t(Maches).
remove_AllLabel_t([]) ->
    {ok, "remove_tag_ok"};
remove_AllLabel_t([Mach = #machine_label{}|T]) ->
    Result = remove_label(Mach#machine_label.id),
    remove_AllLabel_t(T);
remove_AllLabel_t([H|T]) ->
    remove_AllLabel_t(T).

%% @doc 删除所有系统标签
remove_AllSysLabel() ->
    Maches = get_syslabel_label(),
    remove_AllSysLabel_t(Maches).
remove_AllSysLabel_t([]) ->
    {ok, "remove_tag_ok"};
remove_AllSysLabel_t([Mach = #machine_label{}|T]) ->
    Result = remove_label(Mach#machine_label.id),
    remove_AllSysLabel_t(T);
remove_AllSysLabel_t([H|T]) ->
    remove_AllSysLabel_t(T).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


record_to_db(Mach) ->
	[{name,string,list_to_binary(Mach#machine.name)},
	   {host,string,list_to_binary(Mach#machine.host)},
	   {login,string,list_to_binary(Mach#machine.login)},
	   {passwd,string,list_to_binary(Mach#machine.passwd)},
	   {trace,string,list_to_binary(Mach#machine.trace)},
	   {os,string,list_to_binary(Mach#machine.os)},
	   {status,string,list_to_binary(Mach#machine.status)},
	   {method,string,list_to_binary(Mach#machine.method)},
	   {prompt,string,list_to_binary(Mach#machine.prompt)},
	   {loginprom,string,list_to_binary(Mach#machine.loginprom)},
	   {passwdprom,string,list_to_binary(Mach#machine.passwdprom)},
	   {secondprom,string,list_to_binary(Mach#machine.secondprom)},
	   {secondresp,string,list_to_binary(Mach#machine.secondresp)},
	   {initshell,string,list_to_binary(Mach#machine.initshell)},
	   {remoteencoding,string,list_to_binary(Mach#machine.remoteencoding)},	   
	   {sshcommand,string,list_to_binary(Mach#machine.sshcommand)},
	   {sshclient,string,list_to_binary(Mach#machine.sshclient)},
	   {sshport,number,list_to_binary(integer_to_list(Mach#machine.sshport))},
	   {disableconncaching,string,list_to_binary(Mach#machine.disableconncaching)},
	   {connlimit,number,list_to_binary(integer_to_list(Mach#machine.connlimit))},		   
	   {version,string,list_to_binary(Mach#machine.version)},
	   {keyfile,string,list_to_binary(Mach#machine.keyfile)},
	   {sshauthmethod,string,list_to_binary(Mach#machine.sshauthmethod)},
       {label,string,list_to_binary(Mach#machine.label)},
       {type,string,list_to_binary(Mach#machine.type)},
       {other,tuple,term_to_binary(Mach#machine.other)},
       {pwdmode,string,list_to_binary(Mach#machine.pwdmode)}
	 ].
	
valid_value(X)->
X.

valid_value1(X)->
   case X of
       []->
          null;
       {}->
	   null;
	_->
           X
    end.

record_to_ofbizdb(Mach) ->
	[{name,valid_value(Mach#machine.name)},
	   {host,valid_value(Mach#machine.host)},
	   {login,valid_value(Mach#machine.login)},
	   {passwd,valid_value(Mach#machine.passwd)},
	   {trace,valid_value(Mach#machine.trace)},
	   {os,valid_value(Mach#machine.os)},
	   {status,valid_value(Mach#machine.status)},
	   {method,valid_value(Mach#machine.method)},
	   {prompt,valid_value(Mach#machine.prompt)},
	   {loginprom,valid_value(Mach#machine.loginprom)},
	   {passwdprom,valid_value(Mach#machine.passwdprom)},
	   {secondprom,valid_value(Mach#machine.secondprom)},
	   {secondresp,valid_value(Mach#machine.secondresp)},
	   {initshell,valid_value(Mach#machine.initshell)},
	   {remoteencoding,valid_value(Mach#machine.remoteencoding)},	   
	   {sshcommand,valid_value(Mach#machine.sshcommand)},
	   {sshclient,valid_value(Mach#machine.sshclient)},
	   {sshport,valid_value(integer_to_list(Mach#machine.sshport))},
	   {disableconncaching,valid_value(Mach#machine.disableconncaching)},
	   {connlimit,valid_value(integer_to_list(Mach#machine.connlimit))},		   
	   {version,valid_value(Mach#machine.version)},
	   {keyfile,valid_value(Mach#machine.keyfile)},
	   {sshauthmethod,valid_value(Mach#machine.sshauthmethod)},
       {label,valid_value(Mach#machine.label)},
       {type,valid_value(Mach#machine.type)},
       {other,valid_value(Mach#machine.other)},
       {pwdmode,valid_value(Mach#machine.pwdmode)}
	 ].

record_to_ofbizdbold(Mach) ->
	[{name,string,(Mach#machine.name)},
	   {host,string,(Mach#machine.host)},
	   {login,string,(Mach#machine.login)},
	   {passwd,string,(Mach#machine.passwd)},
	   {trace,string,(Mach#machine.trace)},
	   {os,string,(Mach#machine.os)},
	   {status,string,(Mach#machine.status)},
	   {method,string,(Mach#machine.method)},
	   {prompt,string,(Mach#machine.prompt)},
	   {loginprom,string,(Mach#machine.loginprom)},
	   {passwdprom,string,(Mach#machine.passwdprom)},
	   {secondprom,string,(Mach#machine.secondprom)},
	   {secondresp,string,(Mach#machine.secondresp)},
	   {initshell,string,(Mach#machine.initshell)},
	   {remoteencoding,string,(Mach#machine.remoteencoding)},	   
	   {sshcommand,string,(Mach#machine.sshcommand)},
	   {sshclient,string,(Mach#machine.sshclient)},
	   {sshport,number,(integer_to_list(Mach#machine.sshport))},
	   {disableconncaching,string,(Mach#machine.disableconncaching)},
	   {connlimit,number,(integer_to_list(Mach#machine.connlimit))},		   
	   {version,string,(Mach#machine.version)},
	   {keyfile,string,(Mach#machine.keyfile)},
	   {sshauthmethod,string,(Mach#machine.sshauthmethod)},
       {label,string,(Mach#machine.label)},
       {type,string,(Mach#machine.type)},
       {other,tuple,(Mach#machine.other)},
       {pwdmode,string,(Mach#machine.pwdmode)}
	 ].

record_to_ofbizdb1(Mach) ->
	[{name,{string,(Mach#machine.name)}},
	   {host,{string,(Mach#machine.host)}},
	   {login,{string,(Mach#machine.login)}},
	   {passwd,{string,(Mach#machine.passwd)}},
	   {trace,{string,(Mach#machine.trace)}},
	   {os,{string,(Mach#machine.os)}},
	   {status,{string,(Mach#machine.status)}},
	   {method,{string,(Mach#machine.method)}},
	   {prompt,{string,(Mach#machine.prompt)}},
	   {loginprom,{string,(Mach#machine.loginprom)}},
	   {passwdprom,{string,(Mach#machine.passwdprom)}},
	   {secondprom,{string,(Mach#machine.secondprom)}},
	   {secondresp,{string,(Mach#machine.secondresp)}},
	   {initshell,{string,(Mach#machine.initshell)}},
	   {remoteencoding,{string,(Mach#machine.remoteencoding)}},	   
	   {sshcommand,{string,(Mach#machine.sshcommand)}},
	   {sshclient,{string,(Mach#machine.sshclient)}},
	   {sshport,{number,(integer_to_list(Mach#machine.sshport))}},
	   {disableconncaching,{string,(Mach#machine.disableconncaching)}},
	   {connlimit,{number,(integer_to_list(Mach#machine.connlimit))}},		   
	   {version,{string,(Mach#machine.version)}},
	   {keyfile,{string,(Mach#machine.keyfile)}},
	   {sshauthmethod,{string,(Mach#machine.sshauthmethod)}},
       {label,{string,(Mach#machine.label)}},
       {type,{string,(Mach#machine.type)}},
       {other,{tuple,(Mach#machine.other)}},
       {pwdmode,{string,(Mach#machine.pwdmode)}}
	 ].
	 
db_to_machine(Id, Advance) ->
	Data = [dbcs_base:db2term(K, T, V) || {K, T, V} <- Advance],
    Mac = 
	#machine{id=Id, 
			  name=proplists:get_value(name, Data,""),
			  host=proplists:get_value(host, Data,""),
			  login=proplists:get_value(login, Data,""),
			  passwd=proplists:get_value(passwd, Data,""),
			  trace=proplists:get_value(trace, Data,0 ),
			  os=proplists:get_value(os, Data,"nt"),
			  status=proplists:get_value(status, Data,"unknown"),
			  method=proplists:get_value(method, Data,""),
			  prompt=proplists:get_value(prompt, Data,"#"),
			  loginprom=proplists:get_value(loginprom, Data,"login"),
			  passwdprom=proplists:get_value(passwdprom, Data,"password"),
			  secondprom=proplists:get_value(secondprom, Data,""),
			  secondresp=proplists:get_value(secondresp, Data,""),
			  initshell=proplists:get_value(initshell, Data,""),
			  remoteencoding=proplists:get_value(remoteencoding, Data,""),			  
			  sshcommand=proplists:get_value(sshcommand, Data,""),
			  sshclient=proplists:get_value(sshclient, Data,"interJavalib"),
			  sshport=proplists:get_value(sshport, Data,22),
			  disableconncaching=proplists:get_value(disableconncaching, Data,"0"),
			  connlimit=proplists:get_value(connlimit, Data,3),
			  version=proplists:get_value(version, Data,""),
			  keyfile=proplists:get_value(keyfile, Data,""),
			  sshauthmethod=proplists:get_value(sshauthmethod, Data,""),
              label=
                case proplists:get_value(label, Data,0) of
                    undefined ->
                        "";
                    VLabel ->
                        VLabel
                end,
              total=
                case proplists:get_value(total, Data) of
                    undefined ->
                        0;
                    VTotal ->
                        VTotal
                end,
             type = proplists:get_value(type , Data,"SERVER"),
             other=proplists:get_value(other,Data,[]),
             pwdmode=proplists:get_value(pwdmode,Data,"other")
			  },
        NMach = structMacByPwdMode(Mac).
      
ofbizdb_to_machine(Data) ->
%	Data = [dbcs_base:db2term(K, T, V) || {K, T, V} <- Advance],
	%io:format("dbcs machine ofbizdb_to_machine:~p~n",[Data]),
    Mac = 
	#machine{	  id=proplists:get_value(ciId, Data,""),
			  name=proplists:get_value(name, Data,""),
			  host=proplists:get_value(host, Data,""),
			  login=proplists:get_value(login, Data,""),
			  passwd=proplists:get_value(passwd, Data,""),
			  trace=proplists:get_value(trace, Data,0 ),
			  os=proplists:get_value(os, Data,"nt"),
			  status=proplists:get_value(status, Data,"unknown"),
			  method=proplists:get_value(method, Data,""),
			  prompt=proplists:get_value(prompt, Data,"#"),
			  loginprom=proplists:get_value(loginprom, Data,"login"),
			  passwdprom=proplists:get_value(passwdprom, Data,"password"),
			  secondprom=proplists:get_value(secondprom, Data,""),
			  secondresp=proplists:get_value(secondresp, Data,""),
			  initshell=proplists:get_value(initshell, Data,""),
			  remoteencoding=proplists:get_value(remoteencoding, Data,""),			  
			  sshcommand=proplists:get_value(sshcommand, Data,""),
			  sshclient=proplists:get_value(sshclient, Data,"interJavalib"),
			  sshport=proplists:get_value(sshport, Data,22),
			  disableconncaching=proplists:get_value(disableconncaching, Data,"0"),
			  connlimit=proplists:get_value(connlimit, Data,3),
			  version=proplists:get_value(version, Data,""),
			  keyfile=proplists:get_value(keyfile, Data,""),
			  sshauthmethod=proplists:get_value(sshauthmethod, Data,""),
              label=
                case proplists:get_value(label, Data,0) of
                    undefined ->
                        "";
                    VLabel ->
                        VLabel
                end,
              total=
                case proplists:get_value(total, Data) of
                    undefined ->
                        0;
                    VTotal ->
                        VTotal
                end,
             type = proplists:get_value(type , Data,"SERVER"),
             other=proplists:get_value(other,Data,[]),
             pwdmode=proplists:get_value(pwdmode,Data,"other")
			  },
        NMach = structMacByPwdMode(Mac).


machinelabel_to_db(Record) ->
    [
        {id,dbtype(Record#machine_label.id),data2bin(Record#machine_label.id)},
        {name,dbtype(Record#machine_label.name),data2bin(Record#machine_label.name)},
        {type,dbtype(Record#machine_label.type),data2bin(Record#machine_label.type)},
        {index,dbtype(Record#machine_label.index),data2bin(Record#machine_label.index)},
        {syslabel,dbtype(Record#machine_label.syslabel),data2bin(Record#machine_label.syslabel)},
        {hide,dbtype(Record#machine_label.hide),data2bin(Record#machine_label.hide)},
        {value,dbtype(Record#machine_label.value),data2bin(Record#machine_label.value)},
        {treeindex,dbtype(Record#machine_label.treeindex),data2bin(Record#machine_label.treeindex)},
        {maxchild,dbtype(Record#machine_label.maxchild),data2bin(Record#machine_label.maxchild)},
        {parentid,dbtype(Record#machine_label.parentid),data2bin(Record#machine_label.parentid)},
        {childrenid,dbtype(Record#machine_label.childrenid),data2bin(Record#machine_label.childrenid)}
    ]. 

ofbizdb_to_machinelabel(DB) ->
    %Data = [dbcs_base:db2term(K, T, V) || {K, T, V} <- DB], 
    Data = DB,
    %io:format("dbcs machine ofbizdb_to_machinelabel:~p~n",[Data]),
    Name = proplists:get_value(name, Data),
    Type = proplists:get_value(type, Data),
    Index = proplists:get_value(index, Data),
    Syslabel = 
        case proplists:get_value(syslabel, Data) of
            undefined ->
                "false";
            NSyslabel ->
                NSyslabel
        end,
    Id = 
        case proplists:get_value(ciId, Data) of
            undefined ->
                Name;
            NId ->
                NId
        end,
    Hide = 
        case proplists:get_value(hide, Data) of
            undefined ->
                "false";
            NHide ->
                NHide
        end,
    Value = 
        case proplists:get_value(value, Data) of
            undefined ->
                "All";
            NValue ->
                NValue
        end,
    Maxchild = 
        case proplists:get_value(maxchild, Data) of
            undefined ->
                "";
            MaxValue -> MaxValue
        end,
     Treeindex = 
        case proplists:get_value(treeindex, Data) of
            undefined ->
                "";
            TIValue -> TIValue
        end,
    Parentid = 
        case proplists:get_value(parentid, Data) of
            undefined ->
                "";
            PValue -> PValue
        end,
    Childrenid=
    case proplists:get_value(childrenid, Data) of
            undefined ->
                [];
            CHValue -> CHValue
        end,
    #machine_label{ 
        id = Id,
        name = Name,
        type = Type,
        index = Index,
        syslabel = Syslabel,
        hide = Hide,
        value = Value,
        treeindex = Treeindex,
        maxchild = Maxchild,
        parentid = Parentid,
        childrenid = Childrenid
    }.

db_to_machinelabel(DB) ->
    Data = [dbcs_base:db2term(K, T, V) || {K, T, V} <- DB], 
    Name = proplists:get_value(name, Data),
    Type = proplists:get_value(type, Data),
    Index = proplists:get_value(index, Data),
    Syslabel = 
        case proplists:get_value(syslabel, Data) of
            undefined ->
                "false";
            NSyslabel ->
                NSyslabel
        end,
    Id = 
        case proplists:get_value(id, Data) of
            undefined ->
                Name;
            NId ->
                NId
        end,
    Hide = 
        case proplists:get_value(hide, Data) of
            undefined ->
                "false";
            NHide ->
                NHide
        end,
    Value = 
        case proplists:get_value(value, Data) of
            undefined ->
                "All";
            NValue ->
                NValue
        end,
    Maxchild = 
        case proplists:get_value(maxchild, Data) of
            undefined ->
                "";
            MaxValue -> MaxValue
        end,
     Treeindex = 
        case proplists:get_value(treeindex, Data) of
            undefined ->
                "";
            TIValue -> TIValue
        end,
    Parentid = 
        case proplists:get_value(parentid, Data) of
            undefined ->
                "";
            PValue -> PValue
        end,
    Childrenid=
    case proplists:get_value(childrenid, Data) of
            undefined ->
                [];
            CHValue -> CHValue
        end,
    #machine_label{ 
        id = Id,
        name = Name,
        type = Type,
        index = Index,
        syslabel = Syslabel,
        hide = Hide,
        value = Value,
        treeindex = Treeindex,
        maxchild = Maxchild,
        parentid = Parentid,
        childrenid = Childrenid
    }.
    
data2bin(V)when is_integer(V)->list_to_binary(integer_to_list(V));
data2bin(V)when is_float(V)->list_to_binary(float_to_list(V));
data2bin(V)when is_atom(V)->list_to_binary(atom_to_list(V));
data2bin(V)when is_list(V)->
case dbtype(V) of
		string->
			list_to_binary(V);
		_->
			term_to_binary(V)
	end;
data2bin(V)->term_to_binary(V).

dbtype(V) when is_number(V)->number;
dbtype(V) when is_atom(V)->atom;
dbtype(V) when is_list(V)->
	try 
		Tv = binary_to_list(list_to_binary(V)),
		if 
			Tv =:= V ->
				string;
			true->
				tuple
		end
	catch
	_:_->tuple
	end;
dbtype(_)->tuple.


 