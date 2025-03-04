package machinelearning.ml;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet; 
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.DenseInstance;
import weka.core.Instance;

import java.awt.*;

public class ML {

    public static List<AnnonceEmplois> removeDuplicates(List<AnnonceEmplois> annonces) {
        Set<AnnonceEmplois> uniqueSet = new HashSet<>(annonces);
        return new ArrayList<>(uniqueSet);
    }

    public static List<AnnonceEmplois> loadData() throws IOException{
        List<AnnonceEmplois> listeAnnonceEmplois = new ArrayList<AnnonceEmplois>();
        listeAnnonceEmplois.addAll(DataFile1Treatment.parseData());
        listeAnnonceEmplois.addAll(DataFile2Treatment.parseData());
        listeAnnonceEmplois.addAll(DataFile3Treatment.parseData());
        
        Iterator<AnnonceEmplois> iterator = listeAnnonceEmplois.iterator();
        String city;
        String contractType;
        String degree;
        String sector;
        String experience;
        while (iterator.hasNext()) {
            AnnonceEmplois annonceEmplois = iterator.next();
            if (annonceEmplois.getApplicationDeadline() == null || annonceEmplois.getApplicationDeadline().isBlank() ||
            annonceEmplois.getPublicationDate() == null || annonceEmplois.getPublicationDate().isBlank() ||
            annonceEmplois.getCity() == null || annonceEmplois.getCity().isBlank() ||
            annonceEmplois.getContractType() == null || annonceEmplois.getContractType().isBlank() ||
            annonceEmplois.getDegree() == null || annonceEmplois.getDegree().isBlank() ||
            annonceEmplois.getExperience() == null || annonceEmplois.getExperience().isBlank() ||
            annonceEmplois.getSector() == null || annonceEmplois.getSector().isBlank() ||
            annonceEmplois.getRemoteWork() == null || annonceEmplois.getRemoteWork().isBlank()) {
                iterator.remove();
            }
            if(annonceEmplois.getRemoteWork() != null && annonceEmplois.getRemoteWork().equals("Oui 100%")) {
                annonceEmplois.setRemoteWork("Oui");
            }
            if(annonceEmplois.getCity() != null && annonceEmplois.getCity().equals("Côte d'Ivoire")) {
                annonceEmplois.setCity("Cote Ivoire");
            }
            if(annonceEmplois.getSector()!= null && annonceEmplois.getSector().equals("Call Centers  - Secteur Centre d'appel - Conseil")) {
                annonceEmplois.setSector("Call Centers  - Secteur Centre appel - Conseil");
            }
            if(annonceEmplois.getCity() != null){city = Normalizer.normalize(annonceEmplois.getCity(), Normalizer.Form.NFD);
                annonceEmplois.setCity(city.replaceAll("\\p{M}", ""));}
            if(annonceEmplois.getContractType()!= null){
                contractType = Normalizer.normalize(annonceEmplois.getContractType(), Normalizer.Form.NFD);
                annonceEmplois.setContractType(contractType.replaceAll("\\p{M}", ""));
            }
            if(annonceEmplois.getDegree()!= null){
                degree = Normalizer.normalize(annonceEmplois.getDegree(), Normalizer.Form.NFD);
            annonceEmplois.setDegree(degree.replaceAll("\\p{M}", ""));
            }
            if(annonceEmplois.getExperience()!=null){
                experience = Normalizer.normalize(annonceEmplois.getExperience(), Normalizer.Form.NFD);
            annonceEmplois.setExperience(experience.replaceAll("\\p{M}", ""));
            }
            if(annonceEmplois.getSector()!= null) {
                sector = Normalizer.normalize(annonceEmplois.getSector(), Normalizer.Form.NFD);
                annonceEmplois.setSector(sector.replaceAll("\\p{M}", ""));
            }
        }

        List<AnnonceEmplois> uniqueObjectslist = removeDuplicates(listeAnnonceEmplois);
        return uniqueObjectslist;

    }


    public static Connection connectToDatabase(String url, String username, String password) throws SQLException {
         
       return DriverManager.getConnection(url, username, password);

    }

    public static void integrateInDatabase(Connection conn, List<AnnonceEmplois> listeAnnonceEmplois) throws SQLException {
        String insertQuery = "INSERT INTO java.annonce_emplois "
            + "(publicationDate, applicationDeadline, city, contractType, degree, experience, remoteWork, sector) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
        PreparedStatement preparedStatement = null;
    
        try {
            preparedStatement = conn.prepareStatement(insertQuery);
            conn.setAutoCommit(false);
    
            for (AnnonceEmplois annonceEmplois : listeAnnonceEmplois) {
                preparedStatement.setString(1, annonceEmplois.getPublicationDate());
                preparedStatement.setString(2, annonceEmplois.getApplicationDeadline());
                preparedStatement.setString(3, annonceEmplois.getCity());
                preparedStatement.setString(4, annonceEmplois.getContractType());
                preparedStatement.setString(5, annonceEmplois.getDegree());
                preparedStatement.setString(6, annonceEmplois.getExperience());
                preparedStatement.setString(7, annonceEmplois.getRemoteWork());
                preparedStatement.setString(8, annonceEmplois.getSector());
                
                preparedStatement.addBatch();
            }
    
            preparedStatement.executeBatch();
    
            conn.commit();
            System.out.println("Les annonces ont été intégrées avec succès dans la base de données.");
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e; 
        } finally {
           
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }

    public static List<AnnonceEmplois> getAnnonceEmplois(Connection conn) throws SQLException {
        String query = "SELECT publicationDate, applicationDeadline, city, contractType, degree, experience, remoteWork,sector FROM java.annonce_emplois";

        Statement st = conn.createStatement();
        ResultSet résultatReq = st.executeQuery(query);

        List<AnnonceEmplois> annoncesEmplois = new ArrayList<>();

        while(résultatReq.next()) {

            AnnonceEmplois annonceEmploi = new AnnonceEmplois(
                résultatReq.getString("publicationDate"),
                résultatReq.getString("applicationDeadline"),
                résultatReq.getString("city"),
                résultatReq.getString("contractType"),
                résultatReq.getString("degree"),
                résultatReq.getString("experience"),
                résultatReq.getString("remoteWork"),
                résultatReq.getString("sector")
            );

            annoncesEmplois.add(annonceEmploi);

        }

        résultatReq.close();
        st.close();
        conn.close();
        return annoncesEmplois;
    }


    
    public static Instances prepareData(List<AnnonceEmplois> listeannoncesEmplois) throws ParseException{

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("publicationDate"));
        attributes.add(new Attribute("applicationDeadline"));

        ArrayList<String> cities = new ArrayList<>();
        cities.add("Casablanca");
        cities.add("Rabat");
        cities.add("Tanger");
        cities.add("Agadir");
        cities.add("Marrakech");
        cities.add("Maroc");
        cities.add("Oujda");
        cities.add("Laayoune");
        cities.add("Fes");
        cities.add("Al Hoceima");
        cities.add("Settat");
        cities.add("Kenitra");
        cities.add("Beni Mellal");
        cities.add("Autres");
        cities.add("Meknes");
        cities.add("Safi");
        cities.add("Senegal");
        cities.add("Cote Ivoire");
        cities.add("indifferente");
        cities.add("Ouad Ed Dahab");
        attributes.add(new Attribute("city", cities));

        ArrayList<String> contractTypes = new ArrayList<>();
        contractTypes.add("Autre");
        contractTypes.add("CDD");
        contractTypes.add("CDI");
        contractTypes.add("Freelance");
        contractTypes.add("Interim");
        contractTypes.add("Stage");
        attributes.add(new Attribute("contractType", contractTypes));

        ArrayList<String> degrees = new ArrayList<>();
        degrees.add("Bac +5 et plus");
        degrees.add("Bac +4");
        degrees.add("Bac +3");
        degrees.add("Bac +2");
        degrees.add("Qualification avant Bac");
        degrees.add("Bac");
        degrees.add("Autodidacte");
        attributes.add(new Attribute("degree", degrees));

        ArrayList<String> experiences = new ArrayList<>();
        experiences.add("De 3 a 5 ans");
        experiences.add("De 1 a 3 ans");
        experiences.add("De 5 a 10 ans");
        experiences.add("De 1 a 3 ans De 3 a 5 ans");
        experiences.add("Debutant");
        experiences.add("Moins de 1 an");
        experiences.add("Moins de 1 an De 1 a 3 ans");
        experiences.add("Debutant Moins de 1 an");
        experiences.add("De 3 a 5 ans De 5 a 10 ans");
        experiences.add("De 10 a 20 ans");
        experiences.add("De 5 a 10 ans De 10 a 20 ans");
        experiences.add("Plus de 20 ans");
        experiences.add("Moins de 1 an De 3 a 5 ans");
        experiences.add("Debutant De 1 a 3 ans");
        experiences.add("De 5 a 10 ans De 5 a 10 ans");
        experiences.add("De 1 a 3 ans De 5 a 10 ans");
        experiences.add("Plus de 20 ans De 10 a 20 ans");
        attributes.add(new Attribute("experience", experiences));
 
        ArrayList<String> remoteWork = new ArrayList<>();
        remoteWork.add("Oui");
        remoteWork.add("Non");
        remoteWork.add("Hybride");
        attributes.add(new Attribute("remoteWork", remoteWork));

        ArrayList<String> sectors = new ArrayList<>();
        sectors.add("Commercial");
        sectors.add("Informatique");
        sectors.add("Enseignement - Secteur Enseignement");
        sectors.add("Gestion projet");
        sectors.add("Call Centers  - Hotellerie");
        sectors.add("Gestion");
        sectors.add("RH");
        sectors.add("Multimedia");
        sectors.add("Avocat");
        sectors.add("Assurance  - Banque  - Secteur Assurance");
        sectors.add("Banque  - Commercial");
        sectors.add("Sante");
        sectors.add("Hotellerie");
        sectors.add("Banque  - Informatique");
        sectors.add("Methodes");
        sectors.add("Achats");
        sectors.add("Audit");
        sectors.add("Communication");
        sectors.add("Production");
        sectors.add("Environnement  - Travaux");
        sectors.add("Travaux");
        sectors.add("Assistanat de Direction");
        sectors.add("Electricite - Travaux");
        sectors.add("Agriculture  - Travaux");
        sectors.add("Medical");
        sectors.add("Environnement  - Gestion projet");
        sectors.add("Administration des ventes");
        sectors.add("Banque  - Gestion");
        sectors.add("Call Centers  - Commercial");
        sectors.add("Telecoms");
        sectors.add("Agriculture  - Secteur Agroalimentaire - Chimie");
        sectors.add("Environnement  - Secteur Agroalimentaire - Chimie");
        sectors.add("Distribution  - Hotellerie");
        sectors.add("Imprimerie  - Multimedia");
        sectors.add("Call Centers  - Marketing - Secteur Centre d'appel - Communication");
        sectors.add("Banque  - Secteur Banque");
        sectors.add("Banque  - RH");
        sectors.add("Agriculture  - Sante");
        sectors.add("Call Centers  - Secteur Assurance");
        sectors.add("Call Centers  - Secteur Centre d'appel - Offshoring");
        sectors.add("Electricite - Methodes");
        sectors.add("Assurance  - Gestion projet");
        sectors.add("Immobilier");
        sectors.add("Marketing - Secteur Agence pub");
        sectors.add("Call Centers  - Communication");
        sectors.add("Environnement  - Methodes");
        sectors.add("Marketing - Secteur Import");
        sectors.add("Logistique");
        sectors.add("Marketing - Secteur Internet");
        sectors.add("Assurance  - Avocat");
        sectors.add("Agriculture  - Gestion projet");
        sectors.add("Electricite - Informatique");
        sectors.add("Responsable de Departement - Secteur Enseignement");
        sectors.add("Call Centers  - Informatique");
        sectors.add("- Secteur Extraction");
        sectors.add("Distribution  - Commercial");
        sectors.add("Electricite - Gestion projet");
        sectors.add("Call Centers  - Secteur Centre appel - Conseil");
        sectors.add("Enseignement - Medical");
        sectors.add("Marketing - Secteur Enseignement");
        sectors.add("Call Centers  - Telecoms");
        sectors.add("Banque  - Marketing - Secteur Agence pub");
        sectors.add("Call Centers  - Secteur Offshoring");
        sectors.add("Marketing - Secteur Automobile");
        sectors.add("Imprimerie  - Secteur Internet");
        sectors.add("Electricite - Production");
        sectors.add("Call Centers  - Responsable de Departement - Secteur Offshoring");
        sectors.add("Journalisme");
        sectors.add("Tourisme  - Secteur Tourisme");
        sectors.add("Coursier");
        sectors.add("Agriculture  - Enseignement - Secteur Agriculture");
        sectors.add("Assurance  - Secteur Assurance");
        sectors.add("Environnement  - Secteur Agriculture");
        sectors.add("Environnement  - Production");
        sectors.add("Distribution  - Secteur BTP");
        sectors.add("Distribution  - Secteur Energie - Petrole");
        sectors.add("Environnement  - Sante");
        sectors.add("Marketing - Secteur Cosmetique");
        sectors.add("Distribution  - Gestion");
        sectors.add("Assurance  - Informatique");
        sectors.add("Agriculture  - Production");
        sectors.add("Agriculture  - Secteur Agriculture");
        sectors.add("Assurance  - Gestion");
        sectors.add("Electricite - Secteur Automobile");
        sectors.add("Direction Financiere - Direction de la Strategie");
        sectors.add("Enseignement - Sante");
        sectors.add("Cameraman");
        sectors.add("Enseignement - Secteur Conseil");
        sectors.add("Call Centers  - RH");
        sectors.add("Environnement  - Secteur Extraction");
        sectors.add("Urbanisme");
        sectors.add("Marketing - Communication");
        sectors.add("Enseignement - Gestion projet");
        sectors.add("Direction des Operations - Secteur Hotellerie");
        sectors.add("Direction Financiere - Secteur Immobilier");
        sectors.add("Direction des Ressources Humaines - Secteur Hotellerie");
        attributes.add(new Attribute("sector", sectors)); 
    
        Instances dataSet = new Instances("AnnoncesEmploi", attributes, listeannoncesEmplois.size());
        dataSet.setClassIndex(dataSet.attribute("degree").index());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date referenceDate = sdf.parse("1970-01-01");


        for (AnnonceEmplois annonce : listeannoncesEmplois) {
            double[] instanceValues = new double[dataSet.numAttributes()];

            Date pubDate = sdf.parse(annonce.getPublicationDate());
            Date appDate = sdf.parse(annonce.getApplicationDeadline());
            
            long diffPub = pubDate.getTime() - referenceDate.getTime();
            long diffApp = appDate.getTime() - referenceDate.getTime();
    
            instanceValues[0] = TimeUnit.MILLISECONDS.toDays(diffPub);
            instanceValues[1] = TimeUnit.MILLISECONDS.toDays(diffApp);
            instanceValues[2] = dataSet.attribute(2).indexOfValue(annonce.getCity());
            instanceValues[3] = dataSet.attribute(3).indexOfValue(annonce.getContractType());
            instanceValues[4] = dataSet.attribute(4).indexOfValue(annonce.getDegree());
            instanceValues[5] = dataSet.attribute(5).indexOfValue(annonce.getExperience());
            instanceValues[6] = dataSet.attribute(6).indexOfValue(annonce.getRemoteWork()); 
            instanceValues[7] = dataSet.attribute(7).indexOfValue(annonce.getSector()); 

            dataSet.add(new DenseInstance(1.0, instanceValues));
        }

        return dataSet;
    }

    public static void evaluateJ48Model(J48 model,Instances dataforml) throws Exception{
        Evaluation evaluation = new Evaluation(dataforml);
        evaluation.crossValidateModel(model, dataforml, 4, new java.util.Random(1));
        System.out.println("Évaluation du modèle :");
        System.out.println(evaluation.toSummaryString());
    }
    
    public static J48 trainJ48Model(Instances dataforml) throws Exception {
        J48 model = new J48();
        model.buildClassifier(dataforml);
        System.out.println(model);

        return model;
    }

    public static Instance createInstance(Instances dataSet, String publicationDate, String applicationDeadline, String city, String contractType, String experience, String remoteWork, String sector) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date referenceDate = sdf.parse("1970-01-01");

        double[] instanceValues = new double[dataSet.numAttributes()];

        Date pubDate = sdf.parse(publicationDate);
        Date appDate = sdf.parse(applicationDeadline);
        instanceValues[0] = TimeUnit.MILLISECONDS.toDays(pubDate.getTime() - referenceDate.getTime());
        instanceValues[1] = TimeUnit.MILLISECONDS.toDays(appDate.getTime() - referenceDate.getTime());

        instanceValues[2] = dataSet.attribute(2).indexOfValue(city);
        instanceValues[3] = dataSet.attribute(3).indexOfValue(contractType);
        instanceValues[6] = dataSet.attribute(6).indexOfValue(remoteWork);
        instanceValues[5] = dataSet.attribute(5).indexOfValue(experience);
        instanceValues[7] = dataSet.attribute(7).indexOfValue(sector);

        Instance instance = new DenseInstance(1.0, instanceValues);
        instance.setDataset(dataSet); 
        return instance;
    }

    public static String predict(J48 model, Instance instance) throws Exception {
        double classIndex = model.classifyInstance(instance);
            String predictedClass = instance.classAttribute().value((int) classIndex);
        return predictedClass;
    }

    public static EM performClustering(Instances data) throws Exception {
        data.setClassIndex(-1);
        EM model = new EM();

        model.buildClusterer(data);
        System.out.println("Clusterer construit avec succès :");
        System.out.println(model);
        return model;
    }
    
    public static void evaluateClustringModel(EM model, Instances dataforml) throws Exception{
        double test = ClusterEvaluation.crossValidateModel(model, dataforml, 4, new java.util.Random(1));
		System.out.println(test);
    }

    public static void plotClusters(Instances data, EM model) throws Exception {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (int i = 0; i < model.numberOfClusters(); i++) {
            dataset.addSeries(new XYSeries("Cluster " + i));
        }

        for (int i = 0; i < data.numInstances(); i++) {
            int cluster = model.clusterInstance(data.instance(i));
            double x = data.instance(i).value(1); 
            double y = data.instance(i).value(7); 
            dataset.getSeries(cluster).add(x, y);
        }

        JFreeChart scatterPlot = ChartFactory.createScatterPlot(
                "Le Clustering par EM",
                "Date d'application",
                "Secteur d'activité",
                dataset
        );

        JFrame frame = new JFrame("Clustering Result");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(new ChartPanel(scatterPlot), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

       
}
