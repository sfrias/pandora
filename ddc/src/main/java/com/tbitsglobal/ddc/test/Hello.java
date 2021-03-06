package com.tbitsglobal.ddc.test;

// � ABBYY. 2011.
// SAMPLES code is property of ABBYY, exclusive rights are reserved. 
//
// DEVELOPER is allowed to incorporate SAMPLES into his own APPLICATION and modify it under 
// the  terms of  License Agreement between  ABBYY and DEVELOPER.


// ABBYY FineReader Engine 10 Sample

// This sample shows basic steps of ABBYY FineReader Engine usage:
// Initializing, opening image file, recognition and export.

import com.abbyy.FREngine.*;

public class Hello {

	public static void main( String[] args ) {
		try {
			Hello application = new Hello();
			application.Run();
		} catch( Exception ex ) {
			displayMessage( ex.getMessage() );
		}
	}

	public void Run() {
		// Load ABBYY FineReader Engine
		loadEngine();
		try{
			// Process with ABBYY FineReader Engine
			processWithEngine();
		} finally {
			// Unload ABBYY FineReader Engine
			unloadEngine();
		}
	}

	private void loadEngine() {
		displayMessage( "Initializing Engine..." );
		engine = Engine.Load( SamplesConfig.GetDllFolder(), SamplesConfig.GetDeveloperSN(), null, null );
	}

	private void processWithEngine() {
		try {
			// Setup FREngine
			setupFREngine();

			// Process sample image
			processImage();
		} catch( Exception ex ) {
			displayMessage( ex.getMessage() );
		}
	}

	private void setupFREngine() {
		displayMessage( "Loading predefined profile..." );
		engine.LoadPredefinedProfile( "DocumentConversion_Accuracy" );
		// Possible profile names are:
		//   "DocumentConversion_Accuracy", "DocumentConversion_Speed",
		//   "DocumentArchiving_Accuracy", "DocumentArchiving_Speed",
		//   "BookArchiving_Accuracy", "BookArchiving_Speed",
		//   "TextExtraction_Accuracy", "TextExtraction_Speed",
		//   "FieldLevelRecognition",
		//   "BarcodeRecognition",
		//   "Version9Compatibility",
		//   "Default"
	}

	private void processImage() {
		// Create document
		IFRDocument document = engine.CreateFRDocument();

		try {
			// Add image file to document
			displayMessage( "Loading image..." );
			String imagePath = SamplesConfig.GetSamplesFolder() + "\\SampleImages\\Demo.tif";
			document.AddImageFile( imagePath, null, null );

			// Process document
			displayMessage( "Process..." );
			document.Process( null, null, null );

			// Save results
			displayMessage( "Saving results..." );

			// Save results to rtf with default parameters
			String rtfExportPath = SamplesConfig.GetSamplesFolder() + "\\SampleImages\\Demo.rtf";
			document.Export( rtfExportPath, FileExportFormatEnum.FEF_RTF, null );

			// Save results to pdf using 'balanced' scenario
			IPDFExportParams pdfParams = engine.CreatePDFExportParams();
			pdfParams.setScenario( PDFExportScenarioEnum.PES_Balanced );

			String pdfExportPath = SamplesConfig.GetSamplesFolder() + "\\SampleImages\\Demo.pdf";
			document.Export( pdfExportPath, FileExportFormatEnum.FEF_PDF, pdfParams );
		} finally {
			// Close document
			document.Close();
		}
	}

	private void unloadEngine() {
		displayMessage( "Deinitializing Engine..." );
		engine = null;
		System.gc();
		System.runFinalization();
		Engine.Unload();
	}

	public static void displayMessage( String message ) {
		System.out.println( message );
	}

	private IEngine engine = null;
}
