package main.java.nl.hu.hadoop.BigShuf;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.File;
import java.io.IOException;

public class EnglishWords {

    public static Analyzer analyzer = new Analyzer();

    public static void main(String[] args) throws Exception {

        FileUtils.deleteDirectory(new File("result/english_words"));

        Job job = new Job();
        job.setJarByClass(EnglishWords.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(EnglishWordsMapper.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.waitForCompletion(true);

    }

}

class EnglishWordsMapper extends Mapper<LongWritable, Text, Text, Text> {

    public void map(LongWritable Key, Text value, Context context) throws IOException, InterruptedException {

        String[] words = value.toString().split("\\s");
        if(words.length < 2) return;

        double totalChance = 0.0;
        int sentenceLength = 0;

        for (String s : words) {
            s = s.replaceAll("[^\\p{Alpha}]+","");
            s = s.toLowerCase();

            if(!s.equals("") && !s.equals(" ") && s != null) {
                totalChance += EnglishWords.analyzer.predict(s);
                sentenceLength++;
            }
        }

        System.out.println(totalChance / sentenceLength);
        if((totalChance / sentenceLength) < .45) {
            context.write(new Text("This sentence is very suspicious:"), new Text(value));
        }
    }

}