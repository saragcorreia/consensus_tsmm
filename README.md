# Reconstruction of Tissue Specific Metabolic Models 

A framework to reconstruct tissue-specific metabolic models.
Several methods are implemented, such as  tINIT [1], mCADRE [2], FASTCORE [3] and MBA [4].
With this frameworks is possible to use different data sources in each algorithm.

## Consensus Model
Package: consensus

A consensus metabolic model can be reconstructed based on models obtained by the combination of different algorithms and data sources. The main idea is to build a model starting with the reactions present in most of the models, and iteratively append a set of reactions to the final model so that it will be able to perform all the metabolic tasks given as input to the algorithm.

An example of the input files to the method is present in the Consensus folder present in the Docker image __saracorreia/consensus_tsmm__.

### References
* [1] Agren R, Mardinoglu A, Asplund A, Kampf C, Uhlen M, Nielsen J. Identification of anticancer drugs for hepatocellular carcinoma through personalized genome-scale metabolic modeling. Molecular Systems Biology. 2014;10(3):1--13.
* [2] Wang Y, Eddy Ja, Price ND. Reconstruction of genome-scale metabolic models for 126 human tissues using mCADRE. BMC Systems Biology. 2012;6(1):153.
* [3] Vlassis N, Pacheco MP, Sauter T.Fast reconstruction of compact context-specific metabolic network models. PLoS computational biology. 2014;10(1):e1003424.
* [4] Jerby L, Shlomi T, Ruppin E. Computational reconstruction of tissue-specific metabolic models: application to human liver metabolism. Molecular systems biology. 2010;6(401):401.