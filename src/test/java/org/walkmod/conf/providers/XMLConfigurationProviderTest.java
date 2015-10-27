package org.walkmod.conf.providers;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.walkmod.commands.AddCfgProviderCommand;
import org.walkmod.commands.AddPluginCommand;
import org.walkmod.commands.AddTransformationCommand;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.impl.ConfigurationImpl;

public class XMLConfigurationProviderTest {

	@Test
	public void testVersion1_0() throws Exception {
		XMLConfigurationProvider prov = new XMLConfigurationProvider("src/test/resources/testFiles/walkmod.xml", false);
		Configuration conf = new ConfigurationImpl();
		prov.init(conf);
		prov.load();
		Assert.assertEquals(1, conf.getChainConfigs().size());
		Assert.assertEquals("main-chain", conf.getChainConfigs().iterator().next().getName());
	}

	@Test
	public void testVersion1_1() throws Exception {
		XMLConfigurationProvider prov = new XMLConfigurationProvider("src/test/resources/multimodule/walkmod.xml",
				false);
		Configuration conf = new ConfigurationImpl();
		prov.init(conf);
		prov.load();
		Assert.assertEquals(0, conf.getChainConfigs().size());
		Assert.assertEquals(2, conf.getModules().size());
	}

	@Test
	public void testAddTransformation() throws Exception {
		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
		try {
			prov.createConfig();
			AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
					null, false);
			prov.addTransformationConfig(null, null, command.buildTransformationCfg(), false);

			String content = FileUtils.readFileToString(xml);

			Assert.assertTrue(content.contains("imports-cleaner"));

		} finally {
			xml.delete();
		}

	}

	@Test
	public void testAddTransformationRecursively() throws Exception {
		File aux = new File("src/test/resources/modulesxml");
		File aux1 = new File(aux, "module1");
		File aux2 = new File(aux, "module2");

		aux.mkdirs();
		aux1.mkdir();
		aux2.mkdir();

		File xml = new File(aux, "walkmod.xml");
		File xml1 = new File(aux1, "walkmod.xml");
		File xml2 = new File(aux2, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
		try {
			prov.createConfig();
			List<String> modules = new LinkedList<String>();

			modules.add("module1");
			modules.add("module2");

			prov.addModules(modules);

			AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
					null, true);

			prov.addTransformationConfig(null, null, command.buildTransformationCfg(), true);

			String content = FileUtils.readFileToString(xml);

			Assert.assertTrue(!content.contains("imports-cleaner"));

			content = FileUtils.readFileToString(xml1);
			Assert.assertTrue(content.contains("imports-cleaner"));

			content = FileUtils.readFileToString(xml2);
			Assert.assertTrue(content.contains("imports-cleaner"));

		} finally {
			FileUtils.deleteDirectory(aux);

		}

	}

	@Test
	public void testAddTransformationWithChainAndPath() throws Exception {
		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
		try {
			prov.createConfig();
			AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null,
					"src", null, false);
			prov.addTransformationConfig("mychain", "src", command.buildTransformationCfg(), false);

			String content = FileUtils.readFileToString(xml);

			Assert.assertTrue(content.contains("imports-cleaner") && content.contains("src"));

		} finally {
			xml.delete();
		}

	}

	@Test
	public void testAddTransformationWithPath() throws Exception {
		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
		try {
			prov.createConfig();
			AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null,
					"src", null, false);
			prov.addTransformationConfig(null, "src", command.buildTransformationCfg(), false);

			String content = FileUtils.readFileToString(xml);

			Assert.assertTrue(content.contains("imports-cleaner") && content.contains("src"));

		} finally {
			xml.delete();
		}

	}

	@Test
	public void testAddMultipleTransformationWithPath() throws Exception {
		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
		try {
			prov.createConfig();
			AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null,
					"src", null, false);
			prov.addTransformationConfig(null, "src", command.buildTransformationCfg(), false);

			command = new AddTransformationCommand("license-header", null, false, null, "src", null, false);
			prov.addTransformationConfig(null, "src", command.buildTransformationCfg(), false);

			String content = FileUtils.readFileToString(xml);

			Assert.assertTrue(content.contains("imports-cleaner") && content.contains("license-header")
					&& content.contains("src"));

		} finally {
			xml.delete();
		}

	}

	@Test
	public void testConfigProvidersConfig() throws Exception {
		AddCfgProviderCommand command = new AddCfgProviderCommand("maven", null);

		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
		try {
			prov.createConfig();

			ProviderConfig provCfg = command.build();
			prov.addProviderConfig(provCfg);

			String output = FileUtils.readFileToString(xml);

			System.out.println(output);

			Assert.assertTrue(output.contains("maven"));
		} finally {
			if (xml.exists()) {
				xml.delete();
			}
		}

	}

	@Test
	public void testAddModulesConfig() throws Exception {

		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
		try {
			prov.createConfig();
			List<String> modules = new LinkedList<String>();
			modules.add("module1");
			prov.addModules(modules);

			String output = FileUtils.readFileToString(xml);

			System.out.println(output);

			Assert.assertTrue(output.contains("module1"));
		} finally {
			if (xml.exists()) {
				xml.delete();
			}
		}

	}

	@Test
	public void testRemoveTranformation() throws Exception {
		List<String> list = new LinkedList<String>();
		list.add("imports-cleaner");

		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

		try {
			Configuration conf = new ConfigurationImpl();
			prov.init(conf);

			prov.createConfig();

			AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
					null, false);

			prov.addTransformationConfig(null, null, command.buildTransformationCfg(), false);

			prov.removeTransformations(null, list, false);

			String output = FileUtils.readFileToString(xml);

			Assert.assertTrue(!output.contains("imports-cleaner"));
		} finally {
			if (xml.exists()) {
				xml.delete();
			}
		}
	}

	@Test
	public void testRemoveChainTransformations() throws Exception {
		List<String> list = new LinkedList<String>();
		list.add("imports-cleaner");

		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

		try {
			Configuration conf = new ConfigurationImpl();
			prov.init(conf);

			prov.createConfig();

			AddTransformationCommand command0 = new AddTransformationCommand("license-applier", "mychain", false, null,
					null, null, false);

			prov.addTransformationConfig("mychain", null, command0.buildTransformationCfg(), false);

			AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null,
					null, null, false);

			prov.addTransformationConfig("mychain", null, command.buildTransformationCfg(), false);

			prov.removeTransformations("mychain", list, false);

			String output = FileUtils.readFileToString(xml);

			Assert.assertTrue(!output.contains("imports-cleaner"));

			Assert.assertTrue(output.contains("license-applier"));

			list.add("license-applier");

			prov.removeTransformations("mychain", list, false);

			output = FileUtils.readFileToString(xml);

			Assert.assertTrue(!output.contains("chain"));
		} finally {
			if (xml.exists()) {
				xml.delete();
			}
		}
	}

	@Test
	public void testRemoveTransformationsRecursively() throws Exception {
		List<String> list = new LinkedList<String>();
		list.add("imports-cleaner");

		File aux = new File("src/test/resources/xmlmultimodule");
		aux.mkdirs();
		File module0 = new File(aux, "module0");
		File module1 = new File(aux, "module1");
		File cfg0 = new File(module0, "walkmod.xml");
		File cfg1 = new File(module1, "walkmod.xml");

		module0.mkdir();
		module1.mkdir();

		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

		try {
			Configuration conf = new ConfigurationImpl();
			prov.init(conf);

			prov.createConfig();

			prov.addModules(Arrays.asList("module0", "module1"));

			AddTransformationCommand command0 = new AddTransformationCommand("license-applier", "mychain", false, null,
					null, null, true);

			prov.addTransformationConfig("mychain", null, command0.buildTransformationCfg(), true);

			AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null,
					null, null, true);

			prov.addTransformationConfig("mychain", null, command.buildTransformationCfg(), true);

			prov.removeTransformations("mychain", list, true);

			String output = FileUtils.readFileToString(cfg0);

			Assert.assertTrue(!output.contains("imports-cleaner"));

			Assert.assertTrue(output.contains("license-applier"));

			output = FileUtils.readFileToString(cfg1);

			Assert.assertTrue(!output.contains("imports-cleaner"));

			Assert.assertTrue(output.contains("license-applier"));

		} finally {
			if (aux.exists()) {
				FileUtils.deleteDirectory(aux);
			}
		}
	}

	@Test
	public void testSetWriter() throws Exception {

		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

		try {
			Configuration conf = new ConfigurationImpl();
			prov.init(conf);

			prov.createConfig();

			AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
					null, false);

			prov.addTransformationConfig(null, null, command.buildTransformationCfg(), false);

			prov.setWriter(null, "javalang:string-writer", null);

			String output = FileUtils.readFileToString(xml);

			System.out.println(output);

			Assert.assertTrue(output.contains("javalang:string-writer"));

		} finally {
			if (xml.exists()) {
				xml.delete();
			}
		}
	}

	@Test
	public void testSetReader() throws Exception {

		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

		try {
			Configuration conf = new ConfigurationImpl();
			prov.init(conf);

			prov.createConfig();

			AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", null, false, null, null,
					null, false);

			prov.addTransformationConfig(null, null, command.buildTransformationCfg(), false);

			prov.setReader(null, "walkmod:commons:file-reader", null);

			String output = FileUtils.readFileToString(xml);

			System.out.println(output);

			Assert.assertTrue(output.contains("walkmod:commons:file-reader"));

		} finally {
			if (xml.exists()) {
				xml.delete();
			}
		}
	}

	@Test
	public void testRemovePlugin() throws Exception {
		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

		try {
			Configuration conf = new ConfigurationImpl();
			prov.init(conf);

			prov.createConfig();

			List<String> plugins = new LinkedList<String>();
			plugins.add("org.walkmod:imports-cleaner");

			AddPluginCommand command = new AddPluginCommand(plugins);

			List<PluginConfig> pluginCfgs = command.build();

			prov.addPluginConfig(pluginCfgs.get(0), false);

			String output = FileUtils.readFileToString(xml);

			System.out.println(output);

			Assert.assertTrue(output.contains("imports-cleaner"));

			prov.removePluginConfig(pluginCfgs.get(0));

			output = FileUtils.readFileToString(xml);

			System.out.println(output);

			Assert.assertTrue(!output.contains("imports-cleaner"));

		} finally {
			if (xml.exists()) {
				xml.delete();
			}
		}
	}

	@Test
	public void testAddPluginRecursively() throws Exception {
		File aux = new File("src/test/resources/multmodulexml");
		aux.mkdirs();

		File module0 = new File(aux, "module0");
		module0.mkdir();

		File xml = new File(aux, "walkmod.xml");
		File modulexml = new File(module0, "walkmod.xml");

		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

		try {
			Configuration conf = new ConfigurationImpl();
			prov.init(conf);

			prov.createConfig();

			prov.addModules(Arrays.asList("module0"));

			List<String> plugins = new LinkedList<String>();
			plugins.add("org.walkmod:imports-cleaner");

			AddPluginCommand command = new AddPluginCommand(plugins);

			List<PluginConfig> pluginCfgs = command.build();

			prov.addPluginConfig(pluginCfgs.get(0), true);

			String output = FileUtils.readFileToString(modulexml);

			System.out.println(output);

			Assert.assertTrue(output.contains("imports-cleaner"));

		} finally {
			if (aux.exists()) {
				FileUtils.deleteQuietly(aux);
			}
		}
	}

	@Test
	public void testRemoveModule() throws Exception {
		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);

		try {
			Configuration conf = new ConfigurationImpl();
			prov.init(conf);

			prov.createConfig();

			List<String> modules = new LinkedList<String>();
			modules.add("module1");

			prov.addModules(modules);

			String output = FileUtils.readFileToString(xml);

			System.out.println(output);

			Assert.assertTrue(output.contains("module1"));

			prov.removeModules(modules);

			output = FileUtils.readFileToString(xml);

			System.out.println(output);

			Assert.assertTrue(!output.contains("module1"));

		} finally {
			if (xml.exists()) {
				xml.delete();
			}
		}
	}

	@Test
	public void testRemoveProviders() throws Exception {
		AddCfgProviderCommand command = new AddCfgProviderCommand("maven", null);

		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
		try {
			prov.createConfig();

			ProviderConfig provCfg = command.build();
			prov.addProviderConfig(provCfg);

			String output = FileUtils.readFileToString(xml);

			System.out.println(output);

			Assert.assertTrue(output.contains("maven"));
			List<String> providers = new LinkedList<String>();

			providers.add("maven");
			prov.removeProviders(providers);

			output = FileUtils.readFileToString(xml);
			System.out.println(output);

			Assert.assertTrue(!output.contains("maven"));
		} finally {
			if (xml.exists()) {
				xml.delete();
			}
		}
	}

	@Test
	public void testRemoveChains() throws Exception {
		AddTransformationCommand command = new AddTransformationCommand("imports-cleaner", "mychain", false, null,
				null, null, false);

		File aux = new File("src/test/resources/xml");
		aux.mkdirs();
		File xml = new File(aux, "walkmod.xml");
		XMLConfigurationProvider prov = new XMLConfigurationProvider(xml.getPath(), false);
		try {
			prov.createConfig();

			TransformationConfig transfCfg = command.buildTransformationCfg();
			prov.addTransformationConfig("mychain", null, transfCfg, false);

			String output = FileUtils.readFileToString(xml);

			Assert.assertTrue(output.contains("mychain"));

			List<String> chains = new LinkedList<String>();

			chains.add("mychain");
			prov.removeChains(chains);

			output = FileUtils.readFileToString(xml);
			System.out.println(output);

			Assert.assertTrue(!output.contains("mychain"));
		} finally {
			if (xml.exists()) {
				xml.delete();
			}
		}
	}
}
