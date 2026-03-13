import sbt.*

object AppDependencies {

  val bootstrapPlayVersion = "10.6.0"

  val compile: Seq[ModuleID] = List(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "org.typelevel"                %% "cats-core"                 % "2.13.0",
    "com.neovisionaries"            % "nv-i18n"                   % "1.29",
    "com.github.jknack"             % "handlebars"                % "4.3.1"
  )

  val test: Seq[sbt.ModuleID] = List(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapPlayVersion,
    "org.scalatestplus"      %% "scalacheck-1-18"        % "3.2.19.0",
    "org.scalamock"          %% "scalamock"              % "7.5.5"
  ).map(_ % Test)

  val itDependencies: Seq[ModuleID] = List(
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.21.1",
    "io.swagger.parser.v3"         % "swagger-parser-v3"     % "2.1.38"
  ).map(_ % Test)

}