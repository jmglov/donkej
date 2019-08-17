# donkej

Donkej is a voting tool written by los quesos to help facilitate deciding on
which Rich Hickey talk to watch during our `thursday.clj` lunch.

## Development

To get an interactive development environment run:

```bash
clojure -A:fig:build
```

Or from Emacs, `C-c M-J` (`cider-jack-in-cljs`), then select `figwheel-main` as
the ClojureScript REPL type, then type `:dev` when prompted for the
figwheel-main build.

This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

### Setting your username and AWS credentials

At the moment, we don't integrate with AWS Cognito, so you'll need to set your
username and AWS credentials manually. Once your REPL connects, do the
following:

```clj
(require '[re-frame.core :as rf])
(require '[donkej.events :as events])
(rf/dispatch [::events/refresh-aws-credentials! "ACCESS_KEY" "SECRET_KEY"])
(rf/dispatch [::events/set-username "YOUR.USERNAME"])
```

Now you can click the little reload talks icon just above the submitted talks
table.

## Cleaning and releasing

To clean all compiled files:

    rm -rf target/public

To create a production build run:

	rm -rf target/public
	clojure -A:fig:min


## License

Copyright © 2019 Josh Glover et al.
