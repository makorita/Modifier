import java.io.*;
import java.util.*;

public class IPLoader{
	private IPManager ipm;
	private LinkedList<File> configList;
	
	public IPLoader(IPManager ipm){
		this.ipm=ipm;
	}
	
	public void loadRecursive(String srcDir){
		configList=new LinkedList<File>();
		File rootDir=new File(srcDir);
		recursiveCheck(rootDir);
		for(File curFile:configList){
			//if(!curFile.getName().equals("EJNRYPFW01_act_20161020.txt"))continue;
			loadIP(curFile);
			//break;
		}
	}

	public void recursiveCheck(File curDir){
		File[] childList=curDir.listFiles();
		for(File curFile:childList){
			if(curFile.isDirectory())recursiveCheck(curFile);
			else if(curFile.isFile())configList.add(curFile);
		}
	}
	
	private void loadIP(File curFile){
		try {
			BufferedReader br = new BufferedReader(new FileReader(curFile));
			String line=null;
			String hostname=null;
			String i_f=null;
			while ((line = br.readLine()) != null) {
				//System.out.println(line);
				//if(line.matches(".*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*"))System.out.println(line);;
				
				if(line.matches("hostname .*")){
					hostname=line;
					hostname=hostname.replace("hostname ","");
				}
				if(hostname==null)continue;
				if(line.matches("interface .*")){
					i_f=line;
					i_f=i_f.replace("interface ","");
					if(i_f.matches("FastEthernet.*"))i_f=i_f.replace("FastEthernet","Fa");
					if(i_f.matches("GigabitEthernet.*"))i_f=i_f.replace("GigabitEthernet","Gi");
				}
				if(i_f==null)continue;
				if(line.matches(" ip address \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3} \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3} standby .*")){
					String[] word=line.split(" ");
					String primaryIP=word[3];
					String mask=word[4];
					String secondaryIP=word[6];
					String primaryExplain=primaryIP+"/"+Address.getMaskLength(mask)+"\n";
					primaryExplain+=hostname+":primary "+i_f+"\n";
					String secondaryExplain=secondaryIP+"/"+Address.getMaskLength(mask)+"\n";
					secondaryExplain+=hostname+":secondary "+i_f+"\n";
					ipm.addIP(primaryIP,primaryExplain);
					ipm.addIP(secondaryIP,secondaryExplain);
				}else if(line.matches(" ip address \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3} .*")){
					String[] word=line.split(" ");
					String address=word[3];
					String mask=word[4];
					String explain=null;
					if(line.matches(".* secondary"))explain=address+"/"+Address.getMaskLength(mask)+":secondary\n";
					else explain=address+"/"+Address.getMaskLength(mask)+"\n";
					explain+=hostname+" "+i_f+"\n";
					ipm.addIP(address,explain);
				}else if(line.matches(" standby \\d+ ip .*")){
					String vip=line;
					String[] word=vip.split(" ");
					//System.out.println(word[2]);
					//System.out.println(word[4]);
					String address=word[4];
					String explain=address+"\n";
					explain+=hostname+" "+i_f+"_vip\n";
					ipm.addIP(address,explain);
				}
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}