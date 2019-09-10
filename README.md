# MicroAnalyzer

This project contains a set of modules dealing with different responsibilities of the mining software repositories process.
The MicroAnalyzer framework has a plugin architecture allowing users to extend the framework to support new mining tasks.
As a result researchers and practitioners are not required to create new specific mining tools from scratch for their projects.
The hotspots of the MicroAnalyzer architecture allow plugins such as language parsers, repository source cloners, dataset analyzers, and VCS data extractors.

The MicroAnalyzer framework is described in more detail in my master thesis https://github.com/MicroAnalyzer/master-thesis/blob/master/Master_thesis.pdf

## How To Compile Sources

If you checked out the project from GitHub you can build the project with maven using:

```
mvn clean install
```

## Modules
The framework contains three modules each focusing on a specific part of the mining process. A user of the framework can run
any of the modules separately since they are not dependent on each other, e.g., the cloning module could be used to clone repositories
from a hosting service such as GitHub, or the analysis module could be used to perform analyses on data stored in Hadoop flat files (MapFile
and SequenceFile).

**Cloning**: 

This module is used for cloning remote software repositories into a local directory named _/repositories_. This directory is
created by the framework if it does not exist. Begin the cloning process by running the framework with the following two 
parameters: *-file -source*

Example: 
```
java -cp micro-analyzer.jar joelbits.modules.cloning.CloningModule -file repositories.json -source github
```

Explanation of parameters:

* *-file* is the input file containing names of the repositories to be cloned. The file must contain the full 
names as required by each repository source, e.g., apache/logging-log4j2 for a GitHub repository.
* *-source* The source, e.g., github, of the remote repositories. Must be stated so that the framework uses the
correct cloning plugin.

**Preprocessing**:

This module pre-processes the projects found in the _/repositories_ folder which must exist in the same directory as the 
framework jar is run. There are five possible parameters: *-connector -parser -file -source -dataset* and one optional flag: *-all*

Example: 
```
java -cp micro-analyzer.jar joelbits.modules.preprocessing.PreProcessorModule -connector git -parser java -file metadata.json -source github -dataset jmh_dataset -all
```

Explanation of parameters:

* *-connector* informs which connector should be used to connect to the repositories VCS. The reason for 
using a connector is to be able to collect the history of the repository development. 
* *-parser* represent which language parser should be used to extract the raw data. 
* *-file* is the name of the input file that contains the projects' metadata. 
* *-source* identifies the source of the repositories, i.e., where the metadata file were retrieved from, e.g., github. 
* *-dataset* is optional and will be the name given to the created dataset. If this parameter is left out, a default
name will be given to the created dataset.

Explanation of flags:

* *-all* is optional and if set all code base files matching the parser language will be parsed and persisted as a new dataset. If left out, only files containing microbenchmarks will be parsed.


**Analysis**:

This module is used to analyze datasets created by MicroAnalyzer. A dataset consists of two files; a Hadoop MapFile containing
data on a project level, and a Hadoop SequenceFile containing data on a source code level. The output of the analysis module is
a text file containing the analysis result. There are 4 possible parameters when initiating an analysis process: *-plugin -analysis -output -dataset*

Example: 
```
java -cp micro-analyzer.jar joelbits.modules.analysis.AnalysisModule -plugin jmh -analysis configurations -output configurations.txt -dataset jmh_dataset1,jmh_dataset2
```

Explanation of parameters:

* *-plugin* identifies which analysis plugin to use. Since an analysis plugin may contain multiple analyses the user 
should also add a parameter identifying which specific analysis to run.
* *-analysis* is the specific analysis to run, corresponding to the mapper and reducer implementation parts of the analysis plugin.
* *-output* becomes the name for the created output text file containing the analysis results.
* *-dataset* is optional and if used, it names which specific dataset(s) should be subject for analysis. If 
this parameter is left out the default dataset name will be used (which is the default name for the created dataset after 
preprocessing).
