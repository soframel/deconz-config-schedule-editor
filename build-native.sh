export JAVA_HOME="/opt/mandrel-java21-23.1.0.0-Final"
export GRAALVM_HOME="${JAVA_HOME}"
export PATH="${JAVA_HOME}/bin:${PATH}"
mvn package -Pnative