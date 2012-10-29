package com.googlecode.clearnlp.engine;

import java.io.BufferedReader;
import java.util.List;

import com.googlecode.clearnlp.dependency.AbstractDEPParser;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.dependency.srl.AbstractSRLabeler;
import com.googlecode.clearnlp.morphology.AbstractMPAnalyzer;
import com.googlecode.clearnlp.pos.POSLib;
import com.googlecode.clearnlp.pos.POSNode;
import com.googlecode.clearnlp.pos.POSTagger;
import com.googlecode.clearnlp.predicate.AbstractPredIdentifier;
import com.googlecode.clearnlp.segmentation.AbstractSegmenter;
import com.googlecode.clearnlp.tokenization.AbstractTokenizer;
import com.googlecode.clearnlp.util.pair.Pair;

public class EngineProcess
{
	// ============================= input: reader =============================
	
	static public List<List<String>> getSentences(AbstractSegmenter segmenter, BufferedReader fin)
	{
		return segmenter.getSentences(fin);
	}
	
	// ============================= input: tokens =============================
	
	static public POSNode[] getPOSNodes(Pair<POSTagger[],Double> taggers, List<String> tokens)
	{
		POSNode[] nodes = toPOSNodes(tokens);
		predictPOS(taggers, nodes);

		return nodes;
	}
		
	static public POSNode[] getPOSNodesWithLemmas(Pair<POSTagger[],Double> taggers, AbstractMPAnalyzer analyzer, List<String> tokens)
	{
		POSNode[] nodes = getPOSNodes(taggers, tokens);
		analyzer.lemmatize(nodes);

		return nodes;
	}
		
	static public DEPTree getDEPTree(Pair<POSTagger[],Double> taggers, AbstractMPAnalyzer analyzer, AbstractDEPParser parser, List<String> tokens)
	{
		POSNode[] nodes = getPOSNodesWithLemmas(taggers, analyzer, tokens);
		DEPTree tree = toDEPTree(nodes);
		parser.parse(tree);
			
		return tree;
	}
		
	static public DEPTree getDEPTree(Pair<POSTagger[],Double> taggers, AbstractMPAnalyzer analyzer, AbstractDEPParser parser, AbstractPredIdentifier identifier, AbstractSRLabeler labeler, List<String> tokens)
	{
		DEPTree tree = getDEPTree(taggers, analyzer, parser, tokens);
		predictSRL(identifier, labeler, tree);
			
		return tree;
	}
	
	// ============================= input: sentence =============================
	
	static public List<String> getTokens(AbstractTokenizer tokenizer, String sentence)
	{
		return tokenizer.getTokens(sentence);
	}
	
	static public POSNode[] getPOSNodes(AbstractTokenizer tokenizer, Pair<POSTagger[],Double> taggers, String sentence)
	{
		List<String> tokens = getTokens(tokenizer, sentence);
		return getPOSNodes(taggers, tokens);
	}
	
	static public POSNode[] getPOSNodesWithLemmas(AbstractTokenizer tokenizer, Pair<POSTagger[],Double> taggers, AbstractMPAnalyzer analyzer, String sentence)
	{
		List<String> tokens = getTokens(tokenizer, sentence);
		return getPOSNodesWithLemmas(taggers, analyzer, tokens);
	}
	
	static public DEPTree getDEPTree(AbstractTokenizer tokenizer, Pair<POSTagger[],Double> taggers, AbstractMPAnalyzer analyzer, AbstractDEPParser parser, String sentence)
	{
		List<String> tokens = getTokens(tokenizer, sentence);
		return getDEPTree(taggers, analyzer, parser, tokens);
	}
	
	static public DEPTree getDEPTree(AbstractTokenizer tokenizer, Pair<POSTagger[],Double> taggers, AbstractMPAnalyzer analyzer, AbstractDEPParser parser, AbstractPredIdentifier identifier, AbstractSRLabeler labeler, String sentence)
	{
		List<String> tokens = getTokens(tokenizer, sentence);
		return getDEPTree(taggers, analyzer, parser, identifier, labeler, tokens);
	}
	
	// ============================= input: POSNode[] =============================
	
	static public DEPTree getDEPTree(AbstractMPAnalyzer analyzer, AbstractDEPParser parser, POSNode[] nodes)
	{
		DEPTree tree = toDEPTree(nodes);
		analyzer.lemmatize(tree);
		parser.parse(tree);
		
		return tree;
	}
	
	static public DEPTree getDEPTree(AbstractMPAnalyzer analyzer, AbstractDEPParser parser, AbstractPredIdentifier identifier, AbstractSRLabeler labeler, POSNode[] nodes)
	{
		DEPTree tree = getDEPTree(analyzer, parser, nodes);
		predictSRL(identifier, labeler, tree);
		
		return tree;
	}
	
	// ============================= predict: POSNode[] =============================
	
	static public void predictPOS(Pair<POSTagger[],Double> taggers, POSNode[] nodes)
	{
		POSLib.normalizeForms(nodes);

		if (taggers.o1.length == 1 || taggers.o2 < taggers.o1[0].getCosineSimilarity(nodes))
			taggers.o1[0].tag(nodes);
		else
			taggers.o1[1].tag(nodes);
	}
	
	// ============================= predict: SRL =============================
	
	static public void predictSRL(AbstractPredIdentifier identifier, AbstractSRLabeler labeler, DEPTree tree)
	{
		identifier.identify(tree);
		tree.initSHeads();
		labeler.label(tree);	
	}
	
	// ============================= conversion =============================
	
	static public POSNode[] toPOSNodes(List<String> tokens)
	{
		int i, size = tokens.size();
		POSNode[] nodes = new POSNode[size];
		
		for (i=0; i<size; i++)
			nodes[i] = new POSNode(tokens.get(i));
		
		return nodes;
	}
	
	static public DEPTree toDEPTree(POSNode[] nodes)
	{
		DEPTree tree = new DEPTree();
		int i, size = nodes.length;
		
		for (i=0; i<size; i++)
			tree.add(new DEPNode(i+1, nodes[i]));
		
		return tree;
	}
}
