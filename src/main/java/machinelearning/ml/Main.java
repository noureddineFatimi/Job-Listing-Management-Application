package machinelearning.ml;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import weka.classifiers.trees.J48;
import weka.clusterers.EM;
import weka.core.Instance;
import weka.core.Instances;



public class Main {

    public static void main(String[] args) throws Exception{
        
       // List<AnnonceEmplois> list = ML.loadData();
        
        Connection con = ML.connectToDatabase("jdbc:mysql://localhost:3306/java", "root", "");
        //ML.integrateInDatabase(con, list);

        List<AnnonceEmplois> list = ML.getAnnonceEmplois(con);


        Instances dataSet = ML.prepareData(list);
        
        J48 model = ML.trainJ48Model(dataSet);
        Instance instance = ML.createInstance(dataSet, "2023-12-01", "2023-12-15", "Maroc","CDI", "De 1 a 3 ans", "Non", "Environnement  - Secteur Extraction");
        
        ML.evaluateJ48Model(model, dataSet);
        String predictedRemoteWork = ML.predict(model, instance);
        System.out.println("Prédiction du diplôme requis : " + predictedRemoteWork);

        System.out.println("----------------------------------------------------");

        EM clusterer = ML.performClustering(dataSet);
        ML.evaluateClustringModel(clusterer, dataSet);
        ML.plotClusters(dataSet, clusterer);
    }
    
}