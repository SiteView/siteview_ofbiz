-module(javatest).

-export([count/1,mping/2]).
-define(RECEIVE_TIME_OUT, 10*1000).

count(Node) ->

	Ping = net_adm:ping(Node),
	{admin, 'ofbiz@JOHN'} ! {self(), "count"},

	receive

		{ok, Counter} ->

			io:format("Counter is at value: ~p~n", [Counter])
		after ?RECEIVE_TIME_OUT ->
			case Ping of
				pong ->
					[{error, "time is out. "}];
				pang ->					
					[{error, "Connect Java Node Error! "}]
			end	

	end.

mping(0,Node) -> count(Node);
mping(N,Node) -> count(Node), spawn(fun()->mping(N-1,Node) end).