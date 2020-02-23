import java.io.*;
import java.util.*;

import org.jlab.groot.math.*;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.groot.fitter.ParallelSliceFitter;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.TDirectory;
import org.jlab.clas.physics.Vector3;
import org.jlab.clas.physics.LorentzVector;
import org.jlab.groot.base.GStyle;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;

public class RICH{
	boolean userTimeBased, write_volatile;
	int runNum;
	boolean[] trigger_bits;
	public float EBeam;

	public int nPMTS, nANODES;

	public H2F H_dt_channel;
	public H1F H_dt;
  

	public RICH(int reqR, float reqEb, boolean reqTimeBased, boolean reqwrite_volatile){
        	runNum = reqR;userTimeBased=reqTimeBased;
		write_volatile = reqwrite_volatile;
                EBeam = reqEb;

		nPMTS = 391;
		nANODES = 64;

		trigger_bits = new boolean[32];

		String histitle = String.format("RICH  T_meas - T_calc Photons");
		H_dt = new H1F(String.format("H_RICH_dt"),histitle,400,-50,50);
              	H_dt.setTitle(histitle);
               	H_dt.setTitleX("T_meas - T_calc (ns)");
               	H_dt.setTitleY("counts");

		histitle = String.format("RICH Delta T vs channel");
		H_dt_channel = new H2F(String.format("H_RICH_dt_channel"),histitle,nPMTS*nANODES,0.5,0.5+nPMTS*nANODES, 400, -50, 50);
		H_dt_channel.setTitle(histitle);
                H_dt_channel.setTitleX("channel");
                H_dt_channel.setTitleY("T_meas - T_calc (ns)");
		    
	}
        public void getPhotons(DataBank part, DataBank hadr, DataBank phot, DataBank hits){
		float p_min = 1.5f;
		int pmt, anode, absChannel;
		if (hadr.rows() == 1) {
			int richhadron_index = 0;
			int recparticle_pindex = hadr.getShort("particle_index",richhadron_index);
			int recparticle_pid = part.getInt("pid",recparticle_pindex);
			Vector3 P3 = new Vector3(part.getFloat("px",recparticle_pindex),part.getFloat("py",recparticle_pindex),part.getFloat("pz",recparticle_pindex));
			if ( (recparticle_pid == 11)   && (P3.mag() >= p_min) ) {
				for (int j = 0; j< phot.rows(); j++) {
					int GoodPhoton = 1;
					if ( (phot.getFloat("traced_the",j) == 0) || (phot.getFloat("traced_phi",j) == 0) || (phot.getFloat("traced_EtaC",j) == 0)) GoodPhoton = 0; 

		  			/* Good Cherenkov photon */
		  			if ( (phot.getShort("type",j) == 0) && GoodPhoton == 1) {

		    				/* pointer to the RIC Hit bank */
		    				int richhit_pindex = phot.getShort("hit_index",j);
		    
		    				/* channel info */
		    				pmt = hits.getShort("pmt",richhit_pindex);
		    				anode = hits.getShort("anode",richhit_pindex);
		    				absChannel = anode + (pmt-1)*nANODES;

		    				/* Photon path time from production to the MAPMT */
		    				double PhotonPathTime = phot.getFloat("traced_time",j);

		    				/* Photon start time, calculated at the emission point */
		    				double PhotonStartTime = phot.getFloat("start_time",j);
		    
		    				/* Calculated photon time with respect to the event start time */
		    				double CalcPhotonTime = PhotonStartTime + PhotonPathTime;

		    				/* Calibrated measured time */
		    				double MeasPhotonTime = hits.getFloat("time",richhit_pindex);

		    				/* Time difference */
		    				double DTimeCorr = MeasPhotonTime - CalcPhotonTime;

		    				H_dt.fill(DTimeCorr);
		    				H_dt_channel.fill(absChannel, DTimeCorr);	
					} 
				}
			}
		}
        }    

	public void processEvent(DataEvent event){
		if(event.hasBank("RUN::config")){
			DataBank confbank = event.getBank("RUN::config");
			long TriggerWord = confbank.getLong("trigger",0);
			for (int i = 31; i >= 0; i--) {trigger_bits[i] = (TriggerWord & (1 << i)) != 0;} 
                	DataBank eventBank=null, partBank = null, hadrBank = null, photBank = null, hitBank = null;
                	if(userTimeBased){
                        	if(event.hasBank("REC::Event")) eventBank = event.getBank("REC::Event");
                        	if(event.hasBank("REC::Particle"))partBank = event.getBank("REC::Particle");
                        	if(event.hasBank("RICH::hadrons")) hadrBank = event.getBank("RICH::hadrons");
                        	if(event.hasBank("RICH::photons")) photBank = event.getBank("RICH::photons");
				if(event.hasBank("RICH::hits")) hitBank = event.getBank("RICH::hits");
                	}
                	if(!userTimeBased){
                        	if(event.hasBank("REC::Event"))eventBank = event.getBank("REC::Event");
                        	if(event.hasBank("RECHB::Particle"))partBank = event.getBank("RECHB::Particle");
				if(event.hasBank("RICH::hadrons")) hadrBank = event.getBank("RICH::hadrons");
                                if(event.hasBank("RICH::photons")) photBank = event.getBank("RICH::photons");
                                if(event.hasBank("RICH::hits")) hitBank = event.getBank("RICH::hits");
                	}

			//if( (trigger_bits[1] || trigger_bits[2] || trigger_bits[3] || trigger_bits[4] || trigger_bits[5] || trigger_bits[6]) && partBank!=null)e_part_ind = makeElectron(partBank);
			if (eventBank!=null) {
				if ((eventBank.rows() > 0) && (confbank.rows() > 0)) {
					if(partBank!=null && hadrBank!=null && photBank!=null && hitBank!=null) getPhotons(partBank,hadrBank,photBank,hitBank);
					}
			}
		}
	}
        public void plot() {
		EmbeddedCanvas can_RICH  = new EmbeddedCanvas();
		can_RICH.setSize(3500,4000);
                can_RICH.divide(1,2);
                can_RICH.setAxisTitleSize(18);
                can_RICH.setAxisFontSize(18);
                can_RICH.setTitleSize(18);
                can_RICH.cd(0);can_RICH.draw(H_dt);
		can_RICH.cd(1);can_RICH.draw(H_dt_channel);
                if(runNum>0){
                        if(!write_volatile)can_RICH.save(String.format("plots"+runNum+"/RICH.png"));
                        if(write_volatile)can_RICH.save(String.format("/volatile/clas12/rga/spring18/plots"+runNum+"/RICH.png"));
                        System.out.println(String.format("saved plots"+runNum+"/RICH.png"));
                }
                else{
                        can_RICH.save(String.format("plots/RICH.png"));
                        System.out.println(String.format("saved plots/RICH.png"));
                }

	}
	public static void main(String[] args) {
                System.setProperty("java.awt.headless", "true");
                GStyle.setPalette("kRainBow");
                int count = 0;
                int runNum = 0;
		boolean useTB = true;
		boolean useVolatile = false;
                String filelist = "list_of_files.txt";
                if(args.length>0)runNum=Integer.parseInt(args[0]);
                if(args.length>1)filelist = args[1];
                int maxevents = 20000000;
                if(args.length>2)maxevents=Integer.parseInt(args[2]);
		float Eb =10.2f;//10.6f;
                if(args.length>3)Eb=Float.parseFloat(args[3]);
		if(args.length>4)if(Integer.parseInt(args[4])==0)useTB=false;
                RICH ana = new RICH(runNum,Eb,useTB,useVolatile);
                List<String> toProcessFileNames = new ArrayList<String>();
                File file = new File(filelist);
                Scanner read;
                try {
                        read = new Scanner(file);
                        do { 
                                String filename = read.next();
                                toProcessFileNames.add(filename);

                        }while (read.hasNext());
                        read.close();
                }catch(IOException e){ 
                        e.printStackTrace();
                }   
                int progresscount=0;int filetot = toProcessFileNames.size();
                for (String runstrg : toProcessFileNames) if( count<maxevents ){
                        progresscount++;
                        System.out.println(String.format(">>>>>>>>>>>>>>>> %s",runstrg));
                        File varTmpDir = new File(runstrg);
                        if(!varTmpDir.exists()){System.out.println("FILE DOES NOT EXIST");continue;}
                        System.out.println("READING NOW "+runstrg);
                        HipoDataSource reader = new HipoDataSource();
                        reader.open(runstrg);
                        int filecount = 0;
                        while(reader.hasEvent()&& count<maxevents ) { 
                                DataEvent event = reader.getNextEvent();
                                ana.processEvent(event);
                                filecount++;count++;
                                if(count%10000 == 0) System.out.println(count/1000 + "k events (this is RICH analysis in "+runstrg+") ; progress : "+progresscount+"/"+filetot);
                        }   
                        reader.close();
                }   
                System.out.println("Total : " + count + " events");
                ana.plot();
	        ana.write();
        }   

	public void write() {
		TDirectory dirout = new TDirectory();
		dirout.mkdir("/RICH/");
		dirout.cd("/RICH/");
		dirout.addDataSet(H_dt,H_dt_channel);

		if(!write_volatile){
			if(runNum>0)dirout.writeFile("plots"+runNum+"/out_RICH"+runNum+".hipo");
			else dirout.writeFile("plots/out_RICH.hipo");
		}
	}
}
