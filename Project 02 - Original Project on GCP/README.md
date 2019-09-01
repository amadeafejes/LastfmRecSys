# Move Spark job to Google Cloud Platform

## Steps
Main goal of this project is to move the original recommender system (see Project 01) into the cloud. In order to manage as many process as possible in the cloud these steps should be taken:
* Move source data files into a cloud storage
* Populate a relational database managed on the cloud
* Use a compute engine that runs Spark jobs

## Toolset
Choosing Google Cloud Platform these tools would be a good choice:
* Cloud Storage
* Cloud SQL uses MySQL
* Dataproc cluster

## Input data
User profile data and event log data in tsv format on Storage.

## Data pipeline
Run scala spark jobs on dataproc: reads from Storage writes to Storage or cloud sql.
