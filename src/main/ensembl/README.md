# Ensembl species-prefix TSV

Maps Ensembl stable-ID **species prefixes** to species names so `SpeciesInferrer` can resolve species without calling the Ensembl REST API (GXA-1028).

## Source

- Official table: [Ensembl stable ID prefixes](https://www.ensembl.org/info/genome/stable_ids/prefixes.html)
- Ensembl does not publish this table as CSV or REST; the HTML page is the canonical list.
- The checked-in file was built from that table (vertebrates / release-116-era content). Where Ensembl lists the same prefix for multiple strains, the TSV keeps **one row per prefix** (reference species).

## Repo path

```
atlas-web-core/src/main/ensembl/ensembl-species-prefixes.tsv
```

## Format

TSV with columns `Prefix` and `Species name`. A header row is allowed; `EnsemblSpeciesTsvParser` skips lines whose first field does not start with `ENS` or `MGP_`.

## Runtime / NFS location

GXA mounts bioentity files from the Isilon **services** volume. On Codon datamover nodes that is:

```
/nfs/public/services/fg/atlas/bioentity_properties/
```

Same directory as `go-po.id-term-depth.tsv` and `interpro.term-id-type.tsv`.

Layout (matches GO/PO and InterPro):

| Path | Role |
|------|------|
| `.../bioentity_properties/ensembl-species-prefixes.tsv` | Real file |
| `.../bioentity_properties/gxa/ensembl-species-prefixes.tsv` | Symlink to the real file |

The webapp mounts `fg/atlas/bioentity_properties/gxa` as `/atlas-data/bioentity_properties`, so at runtime the file is:

```
/atlas-data/bioentity_properties/ensembl-species-prefixes.tsv
```

Configured in `BasePathsConfig.ensemblSpeciesFilePath()`.

## Deploy (manual)

Codon **login** nodes do not mount `/nfs/public/services`. Use a **datamover** node (Slurm partition `datamover` / `datamover_debug`) and write as `fg_atlas`:

```bash
scp atlas-web-core/src/main/ensembl/ensembl-species-prefixes.tsv \
  codon-login.ebi.ac.uk:~/ensembl-species-prefixes.tsv

ssh codon-login.ebi.ac.uk
srun -p datamover_debug -c 1 --mem=1G --time=00:05:00 bash -lc '
  BASE=/nfs/public/services/fg/atlas/bioentity_properties
  sudo -n -u fg_atlas cp -f $HOME/ensembl-species-prefixes.tsv \
    $BASE/ensembl-species-prefixes.tsv
  sudo -n -u fg_atlas chmod 644 $BASE/ensembl-species-prefixes.tsv
  sudo -n -u fg_atlas ln -sfn $BASE/ensembl-species-prefixes.tsv \
    $BASE/gxa/ensembl-species-prefixes.tsv
'
```

A running JVM keeps the in-memory map from startup; redeploy or restart the webapp to reload the file.
