package machinelearning.ml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataFile2Treatment {
    
    public static void uniqueValuesof(String champs) throws IOException{
        String filePath = "C:\\Users\\ryane\\Desktop\\TP ENSA S7\\JAVA\\PROJET\\WEB-SCRAPING\\web-scraping\\www.emploi.ma_jobs.json";
        InputStream inputStream = new FileInputStream(filePath);


        ObjectMapper mapper = new ObjectMapper();

        TypeFactory typeFactory = mapper.getTypeFactory();
        List<AnnonceEmplois> listAnnonceEmplois = mapper.readValue(inputStream, typeFactory.constructCollectionType(List.class, AnnonceEmplois.class));

        Set<String> uniqueString = new HashSet<>();

        if(champs.equals("city")) {
            for (AnnonceEmplois annonceEmplois : listAnnonceEmplois) {
                uniqueString.add(annonceEmplois.getCity());
            }
        }

        if(champs.equals("contractType")) {
            for (AnnonceEmplois annonceEmplois : listAnnonceEmplois) {
                uniqueString.add(annonceEmplois.getContractType());
            }
        }
        
        if(champs.equals("degree")) {
            for (AnnonceEmplois annonceEmplois : listAnnonceEmplois) {
                uniqueString.add(annonceEmplois.getDegree());
            }
        }

        if(champs.equals("experience")) {
            for (AnnonceEmplois annonceEmplois : listAnnonceEmplois) {
                uniqueString.add(annonceEmplois.getExperience());
            }
        }

        if(champs.equals("remoteWork")) {
            for (AnnonceEmplois annonceEmplois : listAnnonceEmplois) {
                uniqueString.add(annonceEmplois.getRemoteWork());
            }

        }

        if(champs.equals("sector")) {
            for (AnnonceEmplois annonceEmplois : listAnnonceEmplois) {
                uniqueString.add(annonceEmplois.getSector());
            }

        }


 
    }

    public static List<AnnonceEmplois> parseData() throws IOException{
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("jobs.json");

        ObjectMapper mapper = new ObjectMapper();

        TypeFactory typeFactory = mapper.getTypeFactory();
        List<AnnonceEmplois> listAnnonceEmplois = mapper.readValue(inputStream, typeFactory.constructCollectionType(List.class, AnnonceEmplois.class));

        String attribute;
        
        for (AnnonceEmplois annonceEmplois : listAnnonceEmplois) {

            attribute = annonceEmplois.getCity();
            if(attribute != null) {

                Pattern pattern = Pattern.compile(".* sur ([A-Za-zÀ-ÿ'\\s]+) et région");
                Matcher matcher = pattern.matcher(attribute);

                if (matcher.find()) {
                    String ville = matcher.group(1).trim();
                    annonceEmplois.setCity(ville); 
                } else if (attribute.contains("Tout le Maroc")) {
                    annonceEmplois.setCity("Maroc"); 
                } else if (attribute.contains("Toute la Côte d'Ivoire")) {
                    annonceEmplois.setCity("Côte d'Ivoire");
                }else if (attribute.contains("Tout le Sénégal")) {
                    annonceEmplois.setCity("Sénégal");
                }else if (attribute.contains("Autres régions")) {
                    annonceEmplois.setCity("Autres"); 
                } else if (attribute.contains("indifférente")) {
                    annonceEmplois.setCity("indifférente");
                } else if (attribute.matches("[A-Za-zÀ-ÿ'\\s]+ et région")) {
                    annonceEmplois.setCity(attribute.replace(" et région", "").trim());
                } else {
                    annonceEmplois.setCity(null); 
                }
            }


            attribute = annonceEmplois.getContractType();
            if (attribute != null) {
                Pattern pattern = Pattern.compile("(.*)");
                Matcher matcher = pattern.matcher(attribute);
                if (matcher.find()) { 
                    String typeContrat = matcher.group(1); 
                    annonceEmplois.setContractType(typeContrat.trim());
                } else {
                    annonceEmplois.setContractType(null); 
                }
            }


           
           

            attribute = annonceEmplois.getDegree();
            if (attribute != null) {
                Pattern pattern = Pattern.compile("(.*)");
                Matcher matcher = pattern.matcher(attribute);
                if (matcher.find()) { 
                    String diplome = matcher.group(1);
                    annonceEmplois.setDegree(diplome.trim());
                } else {
                    annonceEmplois.setContractType(null); 
                }
            } 

            attribute = annonceEmplois.getSector();
            if (attribute != null) {
                Pattern pattern = Pattern.compile("(.+?) /");
                Matcher matcher = pattern.matcher(attribute);
    
                if (matcher.find()) {
                    String sector = matcher.group(1).trim();
                    String regex = "\\([^)]*\\)";
                    Pattern parenthesisPattern = Pattern.compile(regex);
                    Matcher parenthesisMatcher = parenthesisPattern.matcher(sector);
    
                    if (parenthesisMatcher.find()) {
                        sector = parenthesisMatcher.replaceAll("").trim();
                    }
    
                    annonceEmplois.setSector(sector);
                } else {
                    annonceEmplois.setSector(null);
                }
            } else {
                annonceEmplois.setSector(null);
            }
            
            attribute = annonceEmplois.getRemoteWork();
            if (attribute != null) {
                Pattern pattern = Pattern.compile("(.*)");
                Matcher matcher = pattern.matcher(attribute);
                if (matcher.find()) { 
                    String remoteWork = matcher.group(1);
                    annonceEmplois.setRemoteWork(remoteWork.trim());
                } else {
                    annonceEmplois.setRemoteWork(null); 
                }
            }


            attribute = annonceEmplois.getExperience();
            if (attribute != null) {
                Pattern pattern = Pattern.compile("(.*)");
                Matcher matcher = pattern.matcher(attribute);
                if (matcher.find()) { 
                    String experience = matcher.group(1);
                    annonceEmplois.setExperience(experience.trim());
                } else {
                    annonceEmplois.setExperience(null); 
                }
            }

            attribute = annonceEmplois.getPublicationDate();
            if (attribute != null) {
                Pattern pattern = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})");
                Matcher matcher = pattern.matcher(attribute);
                if (matcher.find()) {
                    String formattedDate = matcher.group(3) + "-" + matcher.group(2) + "-" + matcher.group(1);
                    annonceEmplois.setPublicationDate(formattedDate);
                } else {
                    annonceEmplois.setPublicationDate(null); 
                }
            }

            attribute = annonceEmplois.getApplicationDeadline();
            if (attribute != null) {
                Pattern pattern = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})");
                Matcher matcher = pattern.matcher(attribute);
                if (matcher.find()) { 
                    String formattedDate = matcher.group(3) + "-" + matcher.group(2) + "-" + matcher.group(1);
                    annonceEmplois.setApplicationDeadline(formattedDate); 
                } else {
                    annonceEmplois.setApplicationDeadline(null); 
                }
            } 


        }

        

        return listAnnonceEmplois;


    }




}
