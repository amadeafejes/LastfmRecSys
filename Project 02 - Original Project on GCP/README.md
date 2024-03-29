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

## Problems to solve

Compared to Project 01 this project has additional tasks to solve due to cloud specific requirements.

### Cloud SQL only accepts csv files as input files

The original lastfm-dataset-1K contains tsv files and tsv file format was used in Project 01 for output files too. The purpose of the output files is to serve as input files for the MySQL database. As Google Data Cloud's SQL service only lets csv files as source some changes are needed.

#### Modify file endings

Change Google Storage file paths to generate csv files not tsv-s. Also replace '\t' separator characters with commas.

#### Handle columns that can contain a comma character

In Project 01 there was another reason to use tsv file format everywere and this was the input data. Comma is included in several fields in the raw tsv files so it was much more simplier not change to csv file format. Moving to GCP now requires to handle these fields to keep the data well-structured and free from errors.

Fields that should be handled:
* registered column in userid-profile.tsv for example "Aug 13, 2006"
* artist name (artname) in userid-timestamp-artid-artname-traid-traname.tsv for example "Blood, Sweat & Tears"
* song title (traname) in userid-timestamp-artid-artname-traid-traname.tsv for example "Boy, Boy, Boy (Switch Remix)"

### Merge csv files which contain favorite tracks data
Unfortunately the outout of the machine learning method is a single file per call. As there are 1000 users in the system 1000 files are created. This is not optimal when importing these csv files to the MySQL database, it would be great if these records are contained by a single csv file.
