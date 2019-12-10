package edu.yu.cs.com1320.project;

import java.io.File;
import java.net.URI;


public abstract class DocumentIO
{
	 protected File baseDir;
	 
	 public DocumentIO(File baseDir)
	 {
		 this.baseDir = baseDir;
	 }
	 
	 public DocumentIO() {}
	
	 /**
	 * @param doc to serialize
	 * @return File object representing file on disk to which document was serialized
	 */
	 public File serialize(Document doc)
	 {
		 
		 return null;
	 }
	 
	 /**
	 * @param uri of doc to deserialize
	 * @return deserialized document object
	 */
	 public Document deserialize(URI uri)
	 {
		 return null;
	 }
}