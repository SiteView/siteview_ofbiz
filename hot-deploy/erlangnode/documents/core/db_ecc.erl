%% ---
%%content store数据库访问
%%
%%---
-module(db_ecc).
-export([open_db/1,close_db/1,get_data/3,get_data/4,get_data2/4,get_data_stat/4,
	get_ofbizdata/3,get_ofbizdata/4,get_ofbizdata2/4,get_ofbizdata_stat/4,update_data/4,insert_data/3,delete_data/3,is_open/1,domain/1]).

-include("config.hrl"). 
% -include("log.hrl").

%-define(ERROR_LOG2(X,Y),void).
-define(ERROR_LOG2(X,Y),io:format(X, Y)).

-define(MBox,eccAdmin).
-define(OfbizNode,ofbiz@itsm).
-define(RECEIVE_TIME_OUT, 10*1000).

domain(undefined) -> "localhost";
domain("localhost") -> "localhost";
domain(Host) when is_atom(Host) -> atom_to_list(Host);
domain(Host) ->
  case string:str(Host,".") of
      0 -> "localhost";
	  Pos ->
			case re:run(Host,"^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$") of
				{match,_}->
					"localhost";
				_->
					lists:sublist(Host,1,Pos-1)
			end
      %{ok,lists:sublist(Host,1,Pos-1),lists:nthtail(Pos,Host)}
      %lists:sublist(Host,1,Pos-1)
  end.
 
open_db(Name) ->
	case net_adm:ping(Name) of
		pong->
			true;
		_->
			false
	end.

is_open(Name)->
	open_db(Name).


close_db(_)->
	ok.
	
print_stack()->
	try 
		throw(stack_trace)
	catch 
	   _:_->
		  erlang:get_stacktrace() 
	end.
	
get_data(DbName, Table, [])->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_data:~p,where:~p~n",[Table,[]]),
%%     io:format("hostname:~p~n",[AppName]),
	%case rpc:call(DbName, content, get, [[{application,Table},{content,Where},"from=0&to=100000"]]) of
    case rpc:call(DbName, content, get, [AppName,Table,"", "from=0&to=100000"]) of
		{ok,{_,_,_,R}}->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_data AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, "", Else]),
			Else
	end;

get_data(DbName, Table, Where)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_data:~p,where:~p~n",[Table,Where]),
	%case rpc:call(DbName, content, get, [[{application,Table},{content,Where},"from=0&to=100000"]]) of
    case rpc:call(DbName, content, get, [AppName, Table, Where,"from=0&to=100000"]) of
		{ok,{_,_,_,R}}->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_data AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, Where, Else]),
			Else
	end.
get_data(DbName, Table, [],Order) when is_list(Order),length(Order)>0->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_data:~p,where:~p,order:~p~n",[Table,[],Order]),
%%     io:format("hostname:~p~n",[AppName]),
	case rpc:call(DbName, content, get, [AppName,Table, "", "from=0&to=100000" ++ "&" ++ Order]) of
    %case rpc:call(DbName, content, get, [[{application,Table},{content,Where},"from=0&to=100000" ++ "&" ++ Order]]) of
		{ok,{_,_,_,R}}->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_data AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, "", Else]),
			Else
	end;
get_data(DbName, Table, Where,Order) when is_list(Order),length(Order)>0->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_data:~p,where:~p,order:~p~n",[Table,Where,Order]),
%%     io:format("hostname:~p~n",[AppName]),
	case rpc:call(DbName, content, get, [AppName, Table, Where,"from=0&to=100000" ++ "&" ++ Order]) of
    %case rpc:call(DbName, content, get, [[{application,Table},{content,Where},"from=0&to=100000" ++ "&" ++ Order]]) of
		{ok,{_,_,_,R}}->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_data AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, Where ,Else]),
			Else
	end;
get_data(DbName, Table, [],Order) when is_list(Order)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_data:~p,where:~p,order:~p~n",[Table,[],Order]),
%%     io:format("hostname:~p~n",[AppName]),
	case rpc:call(DbName, content, get, [AppName, Table, "", "from=0&to=100000"]) of
    %case rpc:call(DbName, content, get, [[{application,Table},{content,Where},"from=0&to=100000"]]) of
		{ok,{_,_,_,R}}->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_data AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, "", Else]),
			Else
	end;
get_data(DbName, Table, Where,Order) when is_list(Order)->
	?ERROR_LOG2("DBLOG--get_data:~p,where:~p,order:~p~n",[Table,Where,Order]),
    AppName = domain(get(hostname)),
	case rpc:call(DbName, content, get, [AppName, Table, Where,"from=0&to=100000"]) of
    %case rpc:call(DbName, content, get, [[{application,Table},{content,Where},"from=0&to=100000"]]) of
		{ok,{_,_,_,R}}->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_data AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, Where, Else]),
			Else
	end;
get_data(_, _, _,_)->{error,parameter_error}.


get_data2(DbName, Table, [],Order) when is_list(Order)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_data2:~p,where:~p,order:~p~n",[Table,[],Order]),
    % io:format("hostname:~p~n",[AppName]),
%%     io:format("get_data DbName:~p~n",[DbName]),
%% 	io:format("get_data Table:~p~n",[Table]),
%% 	io:format("get_data Order:~p~n",[Order]),	
	case rpc:call(DbName, content, get, [AppName, Table, "", Order]) of
		{ok,{_,_,_,R}}->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_data AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, "", Else]),
			Else
	end;
get_data2(DbName, Table, Where,Order) when is_list(Order) andalso is_list(Where)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_data2:~p,where:~p,order:~p~n",[Table,Where,Order]),
    % io:format("hostname:~p~n",[AppName]),
%%     io:format("get_data DbName:~p~n",[DbName]),
%% 	io:format("get_data Table:~p~n",[Table]),
%% 	io:format("get_data Where:Where~p~n",[Where]),
%% 	io:format("get_data Order:~p~n",[Order]),
	case rpc:call(DbName, content, get, [AppName, Table, Where, Order]) of
		{ok,{_,_,_,R}}->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_data AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, Where, Else]),
			Else
	end;
get_data2(_, _, _,_)->{error,parameter_error}.


%% @spec get_data_stat(DbName, Table, Where,Order) -> (Result)
%% DbName = string()
%% Table = string()
%% Where = string()
%% Order = string()
%% Result = #content{}
%% @doc get label by id
%% @doc 根据id获取标签
%%
get_data_stat(DbName, Table, [],Order) when is_list(Order)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_data_stat:~p,where:~p,order:~p~n",[Table,[],Order]),
	case rpc:call(DbName, content, get, [AppName, Table, "", Order]) of
		{ok,R}->
			R;
		Else->
			Else
	end;
get_data_stat(DbName, Table, Where,Order) when is_list(Order) andalso is_list(Where)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_data_stat:~p,where:~p,order:~p~n",[Table,Where,Order]),
	case rpc:call(DbName, content, get, [AppName, Table, Where, Order]) of
		{ok,R}->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_data AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, Where, Else]),
			Else
	end;
get_data_stat(_, _, _,_)->{error,parameter_error}.

insert_data(_,_,{})->{error,parameter_error};
insert_data(DbName,Table,Data)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--insert_data1:~p~n",[Table]),
	?ERROR_LOG2("DBLOG--insert_data1:~p~n",[Data]),
%%     io:format("hostname:~p~n",[{AppName,DbName,Table,Data}]),
    %%Newdata = Data#content{xn_type = list_to_binary(Table),application=list_to_atom(AppName)},
    %io:format("____2______:~p~n",[Newdata]),
	%rpc:call(DbName,content,create,[[{application,Table}],Data]).
    %%rpc:call(DbName,content,create,[AppName, Table, Newdata]).
    [{_,_}, {_,_}, {_, Result}] = ofbizcall(?OfbizNode,?MBox,"dbecc",[{action, "create"}, {table, Table}, {inData, Data}]),
    Result.
 
insert_data(AppName,DbName,Table,Data)->
	?ERROR_LOG2("DBLOG--insert_data2:~p~n",[Table]),
	?ERROR_LOG2("DBLOG--insert_data2:~p~n",[Data]),
    %%Newdata = Data#content{xn_type = list_to_binary(Table),application=list_to_atom(AppName)},
    %%rpc:call(DbName,content,create,[AppName, Table, Newdata]). 
    %%Newdata = Data#content{xn_type = Table,application=AppName},
    [{_,_}, {_,_}, {_, Result}] = ofbizcall(?OfbizNode,?MBox,"dbecc",[{action, "create"}, {table, Table}, {inData, Data}]),
    Result.

update_data(_,_,_,{})->{error,parameter_error};
update_data(DbName,Table,Where,Data)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--update_data1:~p,where:~p~n",[Table,Where]),
	?ERROR_LOG2("DBLOG--update_data1 Data:~p~n",[Data]),
%%     io:format("hostname:~p~n",[AppName]),
    %%Newdata = Data#content{xn_type = list_to_binary(Table),application=list_to_atom(AppName)},
    %io:format("_____1____:~p~n",[Newdata]),
	%%Result = rpc:call(DbName,content,update,[AppName, Table, Where, Newdata]),
	[{_,_}, {_,_}, {_, Result}] = ofbizcall(?OfbizNode,?MBox,"dbecc",[{action, "update"}, {table, Table}, {inData, Data}]),
    %io:format("_____1____:~p~n",[Result]),
    Result.

delete_data(DbName,Table,Where)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--delete_data:~p,where:~p~n",[Table,Where]),
%%     io:format("hostname:~p~n",[AppName]),
	rpc:call(DbName,content,delete,[AppName, Table, Where]).

get_ofbizdata(DbName, Table, [])->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_ofbizdata:~p,where:~p~n",[Table,[]]),
%%     io:format("hostname:~p~n",[AppName]),
	%case rpc:call(DbName, content, get, [[{application,Table},{content,Where},"from=0&to=100000"]]) of
%%    case rpc:call(DbName, content, get, [AppName,Table,"", "from=0&to=100000"]) of
%%	    {ok,{_,_,_,R}}->
    case ofbizcall(?OfbizNode,?MBox,"dbecc",[{action, "read"}, {table, Table}, {inData, ["","from=0&to=100000"]}]) of		
		[{_,_}, {_,_}, {_, R}]->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_ofbizdata AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, "", Else]),
			Else
	end;

get_ofbizdata(DbName, Table, Where)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_ofbizdata:~p,where:~p~n",[Table,Where]),
	%case rpc:call(DbName, content, get, [[{application,Table},{content,Where},"from=0&to=100000"]]) of
%%    case rpc:call(DbName, content, get, [AppName, Table, Where,"from=0&to=100000"]) of
%%		{ok,{_,_,_,R}}->
    case ofbizcall(?OfbizNode,?MBox,"dbecc",[{action, "read"}, {table, Table}, {inData, [Where, "from=0&to=100000"]}]) of
		[{_,_}, {_,_}, {_, R}]->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_ofbizdata AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, Where, Else]),
			Else
	end.
get_ofbizdata(DbName, Table, [],Order) when is_list(Order),length(Order)>0->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_ofbizdata:~p,where:~p,order:~p~n",[Table,[],Order]),
%%     io:format("hostname:~p~n",[AppName]),
%%	case rpc:call(DbName, content, get, [AppName,Table, "", "from=0&to=100000" ++ "&" ++ Order]) of
    %case rpc:call(DbName, content, get, [[{application,Table},{content,Where},"from=0&to=100000" ++ "&" ++ Order]]) of
%%		{ok,{_,_,_,R}}->
	case ofbizcall(?OfbizNode,?MBox,"dbecc",[{action, "read"}, {table, Table}, {inData, ["", "from=0&to=100000" ++ "&" ++ Order]}]) of
		[{_,_}, {_,_}, {_, R}]->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_ofbizdata AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, "", Else]),
			Else
	end;
get_ofbizdata(DbName, Table, Where,Order) when is_list(Order),length(Order)>0->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_ofbizdata:~p,where:~p,order:~p~n",[Table,Where,Order]),
%%     io:format("hostname:~p~n",[AppName]),
%	case rpc:call(DbName, content, get, [AppName, Table, Where,"from=0&to=100000" ++ "&" ++ Order]) of
    %case rpc:call(DbName, content, get, [[{application,Table},{content,Where},"from=0&to=100000" ++ "&" ++ Order]]) of
%		{ok,{_,_,_,R}}->
	case ofbizcall(?OfbizNode,?MBox,"dbecc",[{action, "read"}, {table, Table}, {inData, [Where, "from=0&to=100000" ++ "&" ++ Order]}]) of
		[{_,_}, {_,_}, {_, R}]->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_ofbizdata AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, Where ,Else]),
			Else
	end;

get_ofbizdata(DbName, Table, [],Order) when is_list(Order)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_ofbizdata:~p,where:~p,order:~p~n",[Table,[],Order]),
%%     io:format("hostname:~p~n",[AppName]),
%	case rpc:call(DbName, content, get, [AppName, Table, "", "from=0&to=100000"]) of
    %case rpc:call(DbName, content, get, [[{application,Table},{content,Where},"from=0&to=100000"]]) of
%		{ok,{_,_,_,R}}->
	case ofbizcall(?OfbizNode,?MBox,"dbecc",[{action, "read"}, {table, Table}, {inData, ["", "from=0&to=100000"]}]) of
		[{_,_}, {_,_}, {_, R}]->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_ofbizdata AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, "", Else]),
			Else
	end;
get_ofbizdata(DbName, Table, Where,Order) when is_list(Order)->
	?ERROR_LOG2("DBLOG--get_ofbizdata:~p,where:~p,order:~p~n",[Table,Where,Order]),
    AppName = domain(get(hostname)),
%	case rpc:call(DbName, content, get, [AppName, Table, Where,"from=0&to=100000"]) of
    %case rpc:call(DbName, content, get, [[{application,Table},{content,Where},"from=0&to=100000"]]) of
%		{ok,{_,_,_,R}}->
	case ofbizcall(?OfbizNode,?MBox,"dbecc",[{action, "read"}, {table, Table}, {inData, [Where, "from=0&to=100000"]}]) of
		[{_,_}, {_,_}, {_, R}]->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_ofbizdata AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, Where, Else]),
			Else
	end;
get_ofbizdata(_, _, _,_)->{error,parameter_error}.


get_ofbizdata2(DbName, Table, [],Order) when is_list(Order)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_ofbizdata2:~p,where:~p,order:~p~n",[Table,[],Order]),
    % io:format("hostname:~p~n",[AppName]),
%%     io:format("get_data DbName:~p~n",[DbName]),
%% 	io:format("get_data Table:~p~n",[Table]),
%% 	io:format("get_data Order:~p~n",[Order]),	
%	case rpc:call(DbName, content, get, [AppName, Table, "", Order]) of
%		{ok,{_,_,_,R}}->
	case ofbizcall(?OfbizNode,?MBox,"dbecc",[{action, "read"}, {table, Table}, {inData, ["", Order]}]) of
		[{_,_}, {_,_}, {_, R}]->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_ofbizdata AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, "", Else]),
			Else
	end;
get_ofbizdata2(DbName, Table, Where,Order) when is_list(Order) andalso is_list(Where)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_ofbizdata2:~p,where:~p,order:~p~n",[Table,Where,Order]),
    % io:format("hostname:~p~n",[AppName]),
%%     io:format("get_data DbName:~p~n",[DbName]),
%% 	io:format("get_data Table:~p~n",[Table]),
%% 	io:format("get_data Where:Where~p~n",[Where]),
%% 	io:format("get_data Order:~p~n",[Order]),
%	case rpc:call(DbName, content, get, [AppName, Table, Where, Order]) of
%		{ok,{_,_,_,R}}->
	case ofbizcall(?OfbizNode,?MBox,"dbecc",[{action, "read"}, {table, Table}, {inData, [Where, Order]}]) of
		[{_,_}, {_,_}, {_, R}]->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_ofbizdata AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, Where, Else]),
			Else
	end;
get_ofbizdata2(_, _, _,_)->{error,parameter_error}.


%% @spec get_data_stat(DbName, Table, Where,Order) -> (Result)
%% DbName = string()
%% Table = string()
%% Where = string()
%% Order = string()
%% Result = #content{}
%% @doc get label by id
%% @doc 根据id获取标签
%%
get_ofbizdata_stat(DbName, Table, [],Order) when is_list(Order)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_ofbizdata_stat:~p,where:~p,order:~p~n",[Table,[],Order]),
%	case rpc:call(DbName, content, get, [AppName, Table, "", Order]) of
%		{ok,R}->
	case ofbizcall(?OfbizNode,?MBox,"dbecc",[{action, "read"}, {table, Table}, {inData, ["", Order]}]) of
		[{_,_}, {_,_}, {_, R}]->
			R;
		Else->
			Else
	end;
get_ofbizdata_stat(DbName, Table, Where,Order) when is_list(Order) andalso is_list(Where)->
    AppName = domain(get(hostname)),
	?ERROR_LOG2("DBLOG--get_ofbizdata_stat:~p,where:~p,order:~p~n",[Table,Where,Order]),
%	case rpc:call(DbName, content, get, [AppName, Table, Where, Order]) of
		%{ok,R}->
	case ofbizcall(?OfbizNode,?MBox,"dbecc",[{action, "read"}, {table, Table}, {inData, [Where, Order]}]) of
		[{_,_}, {_,_}, {_, R}]->
			R;
		Else->
			?ERROR_LOG2("DBLOG--get_ofbizdata AppName:~p Table:~p, Where:~p, Else:~p~n",[AppName,Table, Where, Else]),
			Else
	end;
get_ofbizdata_stat(_, _, _,_)->{error,parameter_error}.


ofbizcallold(OfbizNode,MBox,ServiceName, Indata) -> 
	{MBox,OfbizNode} ! {self(),ServiceName,Indata},
    receive
        {ServiceName,Res} -> Res
	after 3000 -> timeout
	end.

ofbizcall(OfbizNode,MBox,ServiceName, Indata) -> 
	Msg = {self(),"OfbizService",ServiceName,Indata},
	%spawn(fun()->rpc(MBox,OfbizNode,Msg) end),
	%io:format("calling ~p~n" ,[ServiceName]),
	Result = ofbizrpc(MBox,OfbizNode,Msg),
	Result.

%%@spec rpc(RegName, Node, Msg) -> Response
%%@type RegName = atom()
%%@type Node = atom()
%%@type Msg = [tuple()]
%%@doc remote process calling for the java node with messages.
ofbizrpc(RegName, Node, Msg) ->
	%THIS:set_attribute(?DEBUG_INFO, "remote process call java node ..."),
	Ping = net_adm:ping(Node),
	{RegName, Node} ! Msg,	
	receive
		{Sender,Ret}->
			Ret;
		{ok,_,Ret}->
			qsort(Ret);
		{error,Err}->
			[{error,Err}];
		{Ret, _, _}->
			[{error, atom_to_list(Ret)}];
		_ ->
			{error,"java node return a unknow error1."}
			
		after ?RECEIVE_TIME_OUT ->
			case Ping of
				pong ->
					[{error, "time is out. "}];
				pang ->					
					[{error, "Connect Java Node Error! "}]
			end
	end.

qsort([]) -> [];
qsort([Pivot|T]) ->
	qsort([X || X <- T, element(2,X) < element(2, Pivot)])
	++ [Pivot] ++
	qsort([X || X <- T, element(2,X) >= element(2,Pivot)]).	