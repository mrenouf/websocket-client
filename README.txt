README - websocket-client

This is a quick and dirty websocket *client* written in very plain Java. It's not elegant
and only supports the hybi-00 [1] draft of the protocol (the one currently implemented in
Chrome and current spoken by *most servers (or at least the ones I plan to use it with).

[1] http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-00

The only dependency right now is Guava, but it's only used for a couple small things. 
I plan to replace those so this has no required third party libraries.

I hacked this toegether in about a day, so it current doesn't have a lot of testing and
presently lacks a way to send to the server, but that's coming shortly (or you can add
it!).

-Mark
