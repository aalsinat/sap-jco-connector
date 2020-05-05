SAP JCo Connector for Mule 4
===========================
This connector allows you integrate your mule applications with SAP ERP Central Component (ECC). 

This connector provides:

- Documented XML Schema generation for IDocs and BAPIs/Functions.

- Send/Receive IDocs to/from SAP in XML format.

- Call/Receive BAPIs/Functions to/from SAP in XML format.

**DATASENSE** is available in all operations to easy data transformation when using Anypoint Studio.

Installation and Usage
----------------------
Add this dependency to your application's pom.xml

```
<groupId>8cc3c35b-b952-4c8b-ba8a-fe0dbe73c325</groupId>
<artifactId>mule-sap-jco-connector</artifactId>
<version>X.X.X</version>
<classifier>mule-plugin</classifier>
```

Add this Anypoint Platform exchange repository to your application's pom.xml

```
<repository>
   <id>exchange-repository</id>
   <name>Exchange Repository</name>
   <url>https://maven.anypoint.mulesoft.com/api/v1/organizations/8cc3c35b-b952-4c8b-ba8a-fe0dbe73c325/maven</url>
   <layout>default</layout>
</repository>
```

Add Anypoint Platform credentials for exchange repository server to your local maven settings file `[USER HOME]/.m2/settings.xml`

```
<servers>
...
	<server>
	   <id>exchange-repository</id>
	   <username>YOUR USERNAME</username>
	   <password>YOUR PASSWORD</password>
	</server>
...
</servers>
```

**NOTE**: You also can install this connector using your **Anypoint Platform credentials** and **Anypoint Studio 7+** by "Search in Exchange..." from **Mule Palette**

Third-party SAP's libraries are required
----------------------------------------
You need to download SAP IDoc and SAP JCo java/native libraries from SAP's repository and optionally install them into your local maven repository for your convenience.

Ignore below steps whether you prefer add these dependencies using **SAP JCo connector's configuration** with **Anypoint Studio 7+**, but ensure SAP's libraries are added as dependencies and shared libraries.

Once SAP's libraries are installed into your local maven repository, add them as depencencies and shared libraries into your mule application's pom:

1. Add SAP's libraries dependencies, where **${sap.jco.version}** is sap libraries' version, **${envClassifier}** is OS/platform identifier and **${type}** is the native library extension:

```
<!-- SAP JCo java library -->
<dependency>
    <groupId>com.sap</groupId>
    <artifactId>com.sap.conn.jco.sapjco3</artifactId>
    <version>${sap.jco.version}</version>
</dependency>
<!-- SAP IDoc java library -->
<dependency>
    <groupId>com.sap</groupId>
    <artifactId>com.sap.conn.idoc.sapidoc3</artifactId>
    <version>${sap.jco.version}</version>
</dependency>
<!-- SAP JCo native library -->
<dependency>
    <groupId>com.sap</groupId>
    <artifactId>com.sap.conn.jco.sapjco3-native</artifactId>
    <version>${sap.jco.version}</version>
    <classifier>${envClassifier}</classifier>
    <type>${envType}</type>
</dependency>
```

2. Add SAP's libraries as **shared libraries** to **mule-maven-plugin** within your application's pom:

```
<plugin>
    <groupId>org.mule.tools.maven</groupId>
    <artifactId>mule-maven-plugin</artifactId>
    <version>${mule.maven.plugin.version}</version>
    <extensions>true</extensions>
    <configuration>
        <sharedLibraries>
            <!-- SAP JCo shared java library -->
            <sharedLibrary>
                <groupId>com.sap</groupId>
                <artifactId>com.sap.conn.jco.sapjco3</artifactId>
            </sharedLibrary>
            <!-- SAP IDoc shared java library -->
            <sharedLibrary>
                <groupId>com.sap</groupId>
                <artifactId>com.sap.conn.idoc.sapidoc3</artifactId>
            </sharedLibrary>
            <!-- SAP JCo shared native library -->
            <sharedLibrary>
                <groupId>com.sap</groupId>
                <artifactId>com.sap.conn.jco.sapjco3-native</artifactId>
            </sharedLibrary>
        </sharedLibraries>
    </configuration>
</plugin>
```

How to install third-party SAP's libraries into your local maven repository 
---------------------------------------------------------------------------
Some samples to install SAP libraries into your local maven repository. 

- Install **sapjco3.jar** and **sapidoc3.jar** java libraries, version 3.0.19:

**IMPORTANT**: sapjco3/sapidoc3 artifactId must contains `com.sap.conn.jco.`/`com.sap.conn.idoc.` respectively to avoid jar name verification error since version 3.0.11.

```
mvn install:install-file -Dfile=sapjco3.jar -DgroupId=com.sap -DartifactId=com.sap.conn.jco.sapjco3 -Dversion=3.0.19 -Dpackaging=jar
mvn install:install-file -Dfile=sapidoc3.jar -DgroupId=com.sap -DartifactId=com.sap.conn.idoc.sapidoc3 -Dversion=3.0.19 -Dpackaging=jar
```

- Install Native library **MacOS 32 bits**, version 3.0.19:

```
mvn install:install-file -DgroupId=com.sap -DartifactId=com.sap.conn.jco.sapjco3-native -Dversion=3.0.19 -Dclassifier=darwinintel -Dpackaging=jnilib -Dfile=./native/osx32/libsapjco3.jnilib
```

- Install Native library **Linux 32 bits**, version 3.0.19:

```
mvn install:install-file -DgroupId=com.sap -DartifactId=com.sap.conn.jco.sapjco3-native -Dversion=3.0.19 -Dclassifier=linuxintel -Dpackaging=so -Dfile=./native/linux32/libsapjco3.so
```

- Install Native library **Windows/dos 32 bits**, version 3.0.19:

```
mvn install:install-file -DgroupId=com.sap -DartifactId=com.sap.conn.jco.sapjco3-native -Dversion=3.0.19 -Dclassifier=ntintel -Dpackaging=dll -Dfile=./native/win32/sapjco3.dll
```

- Install Native library **MacOS 64 bits**, version 3.0.19:

```
mvn install:install-file -DgroupId=com.sap -DartifactId=com.sap.conn.jco.sapjco3-native -Dversion=3.0.19 -Dclassifier=darwinintel64 -Dpackaging=jnilib -Dfile=./native/osx64/libsapjco3.jnilib
```

- Install Native library **Linux x86 64 bits**, version 3.0.19:

```
mvn install:install-file -DgroupId=com.sap -DartifactId=com.sap.conn.jco.sapjco3-native -Dversion=3.0.19 -Dclassifier=linuxx86_64 -Dpackaging=so -Dfile=./native/linux64/libsapjco3.so
```

- Install Native library **Windows/dos AMD 64 bits**, version 3.0.19:

```
mvn install:install-file -DgroupId=com.sap -DartifactId=com.sap.conn.jco.sapjco3-native -Dversion=3.0.19 -Dclassifier=ntamd64 -Dpackaging=dll -Dfile=./native/win64/sapjco3.dll
```
