

name := "ObjectIdRefListDemo"

version := "1.0"

scalaVersion := "2.10.2"

{
  val liftVersion = "2.6-M3"
  libraryDependencies ++= Seq(
    "net.liftweb"            	%% "lift-webkit"                   	% liftVersion,
    "net.liftweb"							%% "lift-wizard"                   	% liftVersion,
    "net.liftweb"							%% "lift-mongodb-record" 						% liftVersion,
    "org.mongodb"  						%% "casbah" 												% "2.5.0",
    "net.liftmodules"					%% "mongoauth_2.6"									% "0.4",
    "org.eclipse.jetty"       % "jetty-webapp"                    % "9.1.0.v20131115" % "container",
    "org.eclipse.jetty"       % "jetty-plus"                      % "9.1.0.v20131115" % "container",
    "com.foursquare"          %% "rogue-field"                    % "2.2.0" intransitive(),
    "com.foursquare"          %% "rogue-core"                     % "2.2.0" intransitive(),
    "com.foursquare"          %% "rogue-lift"                     % "2.2.0" intransitive(),
    "com.foursquare"          %% "rogue-index"                    % "2.2.0" intransitive(),
    "org.slf4j"                   % "slf4j-log4j12"               % "1.7.7"
  )
}

scalacOptions += "-deprecation"

seq(lessSettings:_*)

(LessKeys.filter in (Compile, LessKeys.less)) := "styles.less"

(LessKeys.mini in (Compile, LessKeys.less)) := true

seq(closureSettings:_*)

(ClosureKeys.prettyPrint in (Compile, ClosureKeys.closure)) := false

seq(webSettings :_*)

// add managed resources, where less and closure publish to, to the webapp
(webappResources in Compile) <+= (resourceManaged in Compile)

/************************************************************************/
// Jetty configurations
port in container.Configuration := 8080
/************************************************************************/

