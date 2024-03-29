package org.cloudbus.cloudsim.examples;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * A simple example showing how to create a datacenter with one host and run one
 * cloudlet on it.
 */
public class CloudSimExample1 {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {

	    Log.printLine("Starting CloudSimExample1...");

	    try {
	        // First step: Initialize the CloudSim package.
	        int num_user = 1; // number of cloud users
	        Calendar calendar = Calendar.getInstance();
	        boolean trace_flag = false; // mean trace events

	        // Initialize the CloudSim library
	        CloudSim.init(num_user, calendar, trace_flag);

	        // Second step: Create Datacenters
	        Datacenter datacenter0 = createDatacenter("Datacenter_0");

	        // Third step: Create Broker
	        DatacenterBroker broker = createBroker();
	        int brokerId = broker.getId();

	        // Fourth step: Create one virtual machine
	        vmlist = new ArrayList<Vm>();

	        // Create multiple virtual machines
	        int numVms = 5; // Number of VMs to create
	        for (int i = 0; i < numVms; i++) {
	            int vmid = i;
	            int mips = 1000;
	            long size = 10000; // Image size (MB)
	            int ram = 512 + i * 128; // Incremental RAM for demonstration
	            long bw = 1000;
	            int pesNumber = 1 + i; // Incremental number of CPUs
	            String vmm = "Xen";

	            Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
	            vmlist.add(vm);
	        }

	        // submit vm list to the broker
	        broker.submitVmList(vmlist);

	        // Fifth step: Create one Cloudlet
	        cloudletList = new ArrayList<Cloudlet>();

	     // Create multiple cloudlets (workloads)
	        int numCloudlets = 10; // Number of cloudlets to create
	        for (int i = 0; i < numCloudlets; i++) {
	            long length = 400000 + i * 10000; // Incremental length for demonstration
	            long fileSize = 300;
	            long outputSize = 300;
	            UtilizationModel utilizationModel = new UtilizationModelFull();

	            Cloudlet cloudlet = new Cloudlet(i, length, 1, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
	            cloudlet.setUserId(brokerId);
	            cloudlet.setVmId(i % numVms); // Distribute cloudlets among VMs

	            cloudletList.add(cloudlet);
	        }
	        broker.submitCloudletList(cloudletList);

	        // Sixth step: Starts the simulation
	        CloudSim.startSimulation();

	        CloudSim.stopSimulation();

	        //Final step: Print results when simulation is over
	        List<Cloudlet> newList = broker.getCloudletReceivedList();
	        printSimulationResults(newList);

	        Log.printLine("Datacenter Switzerland finished!");
	    } catch (Exception e) {
	        e.printStackTrace();
	        Log.printLine("Unwanted errors happen");
	    }
	}

	private static void printSimulationResults(List<Cloudlet> list) {
	    DecimalFormat dft = new DecimalFormat("###.##");
	    Log.printLine();
	    Log.printLine("Simulation Setup Details:");
	    Log.printLine("Datacenter: Datacenter_0");
	    Log.printLine("Hosts: 20, each with 8 cores (PEs) and 16 GB RAM");
	    Log.printLine("Time Zone: Switzerland (UTC+1)");
	    Log.printLine("==========================");
	    Log.printLine("Simulation Results:");
	    Log.printLine("Cloudlet ID    STATUS    Data center ID    VM ID    Time    Start Time    Finish Time");

	    for (Cloudlet cloudlet : list) {
	        Log.print("    " + cloudlet.getCloudletId() + "        ");

	        if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
	            Log.printLine("SUCCESS" +
	                    "    " + cloudlet.getResourceId() +
	                    "            " + cloudlet.getVmId() +
	                    "        " + dft.format(cloudlet.getActualCPUTime()) +
	                    "        " + dft.format(cloudlet.getExecStartTime()) +
	                    "        " + dft.format(cloudlet.getFinishTime()));
	        }
	    }
	}


	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 */
	private static Datacenter createDatacenter(String name) {

		List<Host> hostList = new ArrayList<Host>();

	    int mips = 1000; // MIPS for each core
	    int ram = 16384; // host memory (MB)
	    long storage = 1000000; // host storage (MB)
	    int bw = 10000; // bandwidth

	    // Create Hosts (Machines)
	    for (int hostId = 0; hostId < 20; hostId++) { // Creating 20 hosts
	        List<Pe> peList = new ArrayList<Pe>();

	        // Create PEs (CPU Cores) for each Host
	        for (int peId = 0; peId < 8; peId++) { // Each host has 8 cores
	            peList.add(new Pe(peId, new PeProvisionerSimple(mips))); // Add new PE with 1000 MIPS
	        }

	        // Add the Host to the Host list with its PEs
	        hostList.add(new Host(
	                hostId,
	                new RamProvisionerSimple(ram),
	                new BwProvisionerSimple(bw),
	                storage,
	                peList,
	                new VmSchedulerTimeShared(peList) // Using a time-shared scheduler
	        ));
			Log.printLine("Machine"+hostId+"added !");

	    }

	    // Create DatacenterCharacteristics
	    String arch = "x86"; // System architecture
	    String os = "Linux"; // Operating system
	    String vmm = "Xen"; // Hypervisor
	    double time_zone = 1.0; // Time zone this resource located (Switzerland)
	    double cost = 3.0; // Cost per second
	    double costPerMem = 0.05; // Cost per MB of RAM
	    double costPerStorage = 0.001; // Cost per MB of storage
	    double costPerBw = 0.0; // Cost per Mb/s of bandwidth

	    LinkedList<Storage> storageList = new LinkedList<Storage>(); // Not adding SAN devices for now

	    DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
	            arch, os, vmm, hostList, time_zone, cost, costPerMem,
	            costPerStorage, costPerBw);

	    // Create the Datacenter
	    Datacenter datacenter = null;
	    try {
	        datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}

}
