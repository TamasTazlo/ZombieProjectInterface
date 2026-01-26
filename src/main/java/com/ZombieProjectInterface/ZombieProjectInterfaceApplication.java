package com.ZombieProjectInterface;

import com.ZombieProjectInterface.service.ZombieClient;

public class ZombieProjectInterfaceApplication {

	public static void main(String[] args) throws Exception  {
		ZombieClient client = new ZombieClient();
        client.start();

	}

}
