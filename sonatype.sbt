sonatypeProfileName := "pl.matisoft"

pomIncludeRepository := { _ => false }

pomExtra in Global := (
  <scm>
    <url>git@github.com:matiwinnetou/play-hysterix.git</url>
    <connection>scm:git:git@github.com:matiwinnetou/play-hysterix.git</connection>
  </scm>
    <url>https://github.com/matiwinnetou/play-hysterix</url>
    <developers>
      <developer>
        <id>matiwinnetou</id>
        <name>Mateusz Szczap</name>
        <url>https://github.com/matiwinnetou</url>
      </developer>
    </developers>)
