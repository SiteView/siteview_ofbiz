-module(javarpc).

-export([call/4,call/2,gethostname/0]).
-define(RECEIVE_TIME_OUT, 10*1000).
-define(MBox,eccadmin).

call(OfbizNode,MBox,ServiceName, Indata) -> 
	Msg = {self(),"OfbizService",ServiceName,Indata},
	Result = rpc(MBox,OfbizNode,Msg),
	Result.
call(ServiceName, Indata) -> 
	OfbizNode = list_to_atom("ofbiz@"++ gethostname()),
	Msg = {self(),"OfbizService",ServiceName,Indata},
	Result = rpc(?MBox,OfbizNode,Msg),
	Result.

%%@spec rpc(RegName, Node, Msg) -> Response
%%@type RegName = atom()
%%@type Node = atom()
%%@type Msg = [tuple()]
%%@doc remote process calling for the java node with messages.
rpc(RegName, Node, Msg) ->
	%THIS:set_attribute(?DEBUG_INFO, "remote process call java node ..."),
	Ping = net_adm:ping(Node),
	{RegName, Node} ! Msg,	
	receive
		{Sender,Ret}->
			%io:format("Response is ~p~n" ,[Ret]),
			Ret;
		{ok,_,Ret}->
			%io:format("Response is ~p~n" ,[Ret]),
			qsort(Ret);
		{error,Err}->
			[{error,Err}];
		{Ret, _, _}->
			[{error, atom_to_list(Ret)}];
		_ ->
			{error,"java node return a unknow error."}
			
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

gethostname()->
	case inet:gethostname() of
		{ok,Name}->
			Name;
		_->
			"localhost"
	end.