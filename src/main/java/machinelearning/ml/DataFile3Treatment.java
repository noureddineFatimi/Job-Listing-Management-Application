package machinelearning.ml;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class DataFile3Treatment {
     public static void uniqueValuesof(String champs) throws IOException{
         String filePath = "C:\\Users\\ryane\\Desktop\\TP ENSA S7\\JAVA\\PROJET\\WEB-SCRAPING\\web-scraping\\marocemploi_jobs.json";
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
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("marocemploi_jobs.json");

        ObjectMapper mapper = new ObjectMapper();

        TypeFactory typeFactory = mapper.getTypeFactory();
        List<AnnonceEmplois> listAnnonceEmplois = mapper.readValue(inputStream, typeFactory.constructCollectionType(List.class, AnnonceEmplois.class));

        String attribute;
        
        for (AnnonceEmplois annonceEmplois : listAnnonceEmplois) {

            attribute = annonceEmplois.getCity();
            if(attribute != null) {

                Pattern pattern = Pattern.compile("([a-zA-Z]+), .*");
                Matcher matcher = pattern.matcher(attribute);

                if (matcher.find()) {
                    String ville = matcher.group(1).trim();
                    annonceEmplois.setCity(ville); 
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

            attribute = annonceEmplois.getPublicationDate();
            if (attribute != null) {
                Pattern pattern = Pattern.compile("(\\d+) (.+) (\\d{4})");
                Matcher matcher = pattern.matcher(attribute);
                if (matcher.find()) {

                    String mois = null;
                    String jourFormaté = null;

                    if(matcher.group(2).equals("janvier")){mois = "01";}
                    if(matcher.group(2).equals("février")){mois = "02";}
                    if(matcher.group(2).equals("mars")){mois = "03";}
                    if(matcher.group(2).equals("avril")){mois = "04";}
                    if(matcher.group(2).equals("mai")){mois = "05";}
                    if(matcher.group(2).equals("juin")){mois = "06";}
                    if(matcher.group(2).equals("juillet")){mois = "07";}
                    if(matcher.group(2).equals("août")){mois = "08";}
                    if(matcher.group(2).equals("septembre")){mois = "09";}
                    if(matcher.group(2).equals("octobre")){mois = "10";}
                    if(matcher.group(2).equals("novembre")){mois = "11";}
                    if(matcher.group(2).equals("décembre")){mois = "12";}


                    if(matcher.group(1).matches("\\d{1}")) {
                        jourFormaté = "0"+matcher.group(1);
                    } else {
                        jourFormaté = matcher.group(1);
                    }

                    String formattedDate = matcher.group(3) + "-" + mois + "-" + jourFormaté;
                    annonceEmplois.setPublicationDate(formattedDate);
                } else {
                    annonceEmplois.setPublicationDate(null); 
                }
            }

            attribute = annonceEmplois.getApplicationDeadline();
            if (attribute != null) {
                Pattern pattern = Pattern.compile("(\\d+) (.+) (\\d{4})");
                Matcher matcher = pattern.matcher(attribute);
                if (matcher.find()) {

                    String mois = null;
                    String jourFormaté = null;

                    if(matcher.group(2).equals("janvier")){mois = "01";}
                    if(matcher.group(2).equals("février")){mois = "02";}
                    if(matcher.group(2).equals("mars")){mois = "03";}
                    if(matcher.group(2).equals("avril")){mois = "04";}
                    if(matcher.group(2).equals("mai")){mois = "05";}
                    if(matcher.group(2).equals("juin")){mois = "06";}
                    if(matcher.group(2).equals("juillet")){mois = "07";}
                    if(matcher.group(2).equals("août")){mois = "08";}
                    if(matcher.group(2).equals("septembre")){mois = "09";}
                    if(matcher.group(2).equals("octobre")){mois = "10";}
                    if(matcher.group(2).equals("novembre")){mois = "11";}
                    if(matcher.group(2).equals("décembre")){mois = "12";}


                    if(matcher.group(1).matches("\\d{1}")) {
                        jourFormaté = "0"+matcher.group(1);
                    } else {
                        jourFormaté = matcher.group(1);
                    }

                    String formattedDate = matcher.group(3) + "-" + mois + "-" + jourFormaté;
                    annonceEmplois.setApplicationDeadline(formattedDate);
                } else {
                    annonceEmplois.setApplicationDeadline(null); 
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

        }

        return listAnnonceEmplois;

        
    }
}
