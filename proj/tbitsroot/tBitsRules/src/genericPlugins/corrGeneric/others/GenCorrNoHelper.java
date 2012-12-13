package corrGeneric.others;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Type;
import corrGeneric.com.tbitsGlobal.server.managers.CorrNumberManager;
import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.key.CorrNumberKey;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrNumberConfigNotFoundException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class GenCorrNoHelper 
{
	public static TBitsLogger LOG = TBitsLogger.getLogger("impCorr");
	public static String getCorrNo(CorrObject co, Connection con) throws CorrException, CorrNumberConfigNotFoundException 
	{
		/*
		 * cases 
		 * 1 : generate both file and number
		 * 2 : generate only file with given number
		 * 3 : don't generate anything
		 * 4 : generate only number 
		 */
		String genCorr = co.getAsString(co.getFieldNameMap().get(GenericParams.GenerateCorrespondenceFieldName).getBaFieldName());
		FieldNameEntry cfn = co.getFieldNameMap().get(GenericParams.CorrespondenceNumberFieldName);
		String corrFieldName = null ;
		if( null != cfn )
		 corrFieldName = cfn.getBaFieldName() ;
		String corrNo = null;
		
		if( genCorr.equals(GenericParams.GenerateCorr_NoPdforCorrNumber))
			throw new CorrException("You have selected not to generate a correspondence file and correspondence number.");
			
		if( genCorr.equals(GenericParams.GenerateCorr_OnlyPdfWithSpecifiedNumber))
		{
			if( null == corrFieldName )
				throw new CorrException("You have specified to take the number from correspondence number field. But such a field is not configured for this ba : " + co.getBa().getSystemPrefix());
			corrNo = co.getAsString(corrFieldName);
			if( null == corrNo || corrNo.trim().equals(""))
				throw new CorrException("You have selected to generate correspondence file with given number but not provided the correspondence number.") ;
			else
			{
				corrNo = corrNo.trim();
				return validateCorrNumber(con,co,corrNo) ;							
			}
		}
		else //only no. / no. and pdf  
		{
			corrNo = co.getAsString(corrFieldName);
			if( null != corrNo && !corrNo.trim().equals(""))
			{
				throw new CorrException("Please remove any number in field " + Utility.fdn(co.getBa(),corrFieldName) + " as it will be generated by system.");
			}
			corrNo = null ;
			if( co.getSource() == CorrObject.SourcePreview )
				corrNo = getExpectedCorrNo(con,co) ;
			else
			{
				corrNo = getRealCorrNo(co,con) ;
				if(null == corrFieldName)
					throw new CorrException("You have chose to generate correspondence Number. But the correspondence number field is not configured for this BA : " + co.getBa().getSystemPrefix());
				if( null == corrNo )
					throw new CorrException("Excetion occured while generating correspondence number.");
				
			}
			
			if( null == corrNo )
				throw new CorrException("Cannot generate the correspondence number.") ;
			
			
			return corrNo ;
		}
	}
	
	private static String validateCorrNumber(Connection conn, CorrObject co, String corrNo) throws CorrException, CorrNumberConfigNotFoundException 
	{
		Field corrNoField = null;
		try {
			corrNoField = Field.lookupBySystemIdAndFieldName(co.getBa().getSystemId(),co.getFieldNameMap().get(GenericParams.CorrespondenceNumberFieldName).getBaFieldName());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new CorrException(e);
		}
		
		int lastInd = corrNo.lastIndexOf('-');
		if( lastInd == -1 || ( lastInd == corrNo.length() -1 ) ) // number does not contain '-' OR '-' is the last letter of the number ex : 'corrNo-'
		{
			return corrNo ;//throw new CorrException( corrNoField.getDisplayName() + " is not in format of prefix-runningNo.\n The expected number and format is : " + getExpectedCorrNo(conn, co));
		}
		
		String prefix = corrNo.substring(0, lastInd);
		String runningNumber = corrNo.substring(lastInd+1);
		
		String expectedPrefix = getCorrPrefix(co);
		expectedPrefix = expectedPrefix.substring(0, expectedPrefix.lastIndexOf('-'));
		if( !expectedPrefix.equals(prefix) )
			return corrNo ;
			//throw new CorrException("The provided number has prefix : " + prefix + " while the expected prefix according to the parameters is : " + expectedPrefix );
		
		// running number should be integer.
		int runningNo = 0;
		try
		{
			runningNo = Integer.parseInt(runningNumber);
		}
		catch(NumberFormatException nfe)
		{
			LOG.error("The running number : " + runningNumber + " of the " + corrNoField.getDisplayName() + " : " + corrNo + " was not integer. So aborting the rule.");
			throw new CorrException("The running number : " + runningNumber + " of the " + corrNoField.getDisplayName() + corrNo + " was not Integer.");
		}
		
		// no extra sanitization.
		String maxIdName = getCorrMaxIdName(co);
		int maxNumber = getMaxCorrNo(conn, maxIdName);
		if( runningNo > maxNumber + 1 )
		{
			LOG.info("The maximum running number for the correspondence prefix = '" + prefix + "' can be set to : " + (maxNumber +1) );
			throw new CorrException("The maximum running number for the correspondence prefix = '" + prefix + "' can be set to : " + (maxNumber + 1));
		}
		else
		{
			if( runningNo == maxNumber + 1 )
			{
				if( co.getSource() == CorrObject.SourceReal )
				{
					int incrNumber = incrAndGetCorrNo(conn, prefix);
					if( incrNumber != runningNo )
					{
						LOG.error("The max_id was " + maxNumber + " but when incremented to make it according to the given number of :" + runningNo + ", it returned " + incrNumber + " and this mismatch cannot be handled." );
						throw new CorrException("Race condition occurred while finding and incrementing the max number. Please try again for valid results.");
					}
					else
					{
						LOG.info("The increment of number to " + incrNumber + " was correct.");					
					}
				}
			}
			else
				LOG.info("The running number of : " + runningNo + " was less than or equal to : " + maxNumber + " for the prefix : " + prefix + " and is allowed.");
		}
		
		DecimalFormat df = new DecimalFormat("0000") ;
		String runNo = df.format(runningNo);
		
		return expectedPrefix + "-" + runNo;
	}

	public static int getMaxCorrNo( Connection con, String name ) throws CorrException
	{
		String query = "select id from max_ids where name='" + name + "'" ;
		try
		{
			PreparedStatement ps = con.prepareStatement(query) ;
			ResultSet rs = ps.executeQuery() ;
			if( rs.next()  )
			{
				String max_id = rs.getString("id") ;
				if( null == max_id )
				{
					// treat this as 0 
					return 0 ;
				}
				else
				{
					return Integer.parseInt(max_id) ;
				}
			}
			else
			{
				// treat this also as 0
				return 0 ;
			}
		}
		catch(SQLException s)
		{
			throw new CorrException("Cannot find the next correspondence number") ;
		}
		catch(NumberFormatException n )
		{
			throw new CorrException("Cannot find the next correspondence number") ;
		}
		finally
		{
//			if( null != con )
//			{
//				try {
//					con.close() ;
//				} catch (SQLException e) {					
//					e.printStackTrace();
//				}
//			}
		}
	}
	
	public static String getExpectedCorrNo( Connection con, CorrObject co ) throws CorrException, CorrNumberConfigNotFoundException
	{	
		String maxIdName = getCorrMaxIdName(co);
		// generate complete correspondence no.		
		int ncid = getMaxCorrNo( con, maxIdName );
		
		ncid++ ;
		
		CorrNumberEntry cne = getCorrNumberEntry(co);
		Object [] fieldValues = getDecodedString(co,cne.getNumberFields());
		if( null == fieldValues )
			fieldValues = new Object[1] ;
		fieldValues[fieldValues.length-1] = ncid;
		
		return "[Likely]" + String.format(cne.getNumberFormat(), fieldValues);
	}
	
	public static int incrAndGetCorrNo(Connection con, String corrCat ) throws CorrException
	{
		System.out.println("generating corr. no. for : " + corrCat );
		try {	
			CallableStatement stmt = con.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, corrCat );
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("max_id");
				System.out.println("Returning the next corr. no. = " + id );
				return id;
			} else {
				throw new CorrException("Cannot generate the next correspondence number");
			}
		} catch (SQLException e) {
			throw new CorrException("Cannot generate the next correspondence number");
		}		
	}
	
	public static String getRealCorrNo(CorrObject co, Connection con) throws CorrException, CorrNumberConfigNotFoundException 
	{
		String maxIdName = getCorrMaxIdName(co);
		int ncid = incrAndGetCorrNo(con,maxIdName);
				
		CorrNumberEntry cne = getCorrNumberEntry(co);
		Object [] fieldValues = getDecodedString(co,cne.getNumberFields());
		if(null == fieldValues)
			fieldValues = new Object[1];
		
		fieldValues[fieldValues.length-1] = ncid;
		
		return String.format(cne.getNumberFormat(), fieldValues);
	}
	
	public static String getCorrMaxIdName(CorrObject co) throws CorrException, CorrNumberConfigNotFoundException
	{
		// get corr-prefix from the database configurations.
		HashMap<CorrNumberKey, CorrNumberEntry> configs = CorrNumberManager.getInstance().getCorrNumberCache().get(co.getBa().getSystemPrefix());
		
		CorrNumberKey cnk = getCorrNumberKey( co );
		
		if( null == configs )
			throw new CorrNumberConfigNotFoundException("No corr number generation configuration found for ba : " + co.getBa().getSystemPrefix());
		
		CorrNumberEntry cne = configs.get(cnk);
		
		if( null == cne )
		{	
			// find the default key i.e. with all types null.
			CorrNumberKey c = new CorrNumberKey(cnk.getSysPrefix(), null, null, null);
			cne = configs.get(c);
			if( null == cne )
				throw new CorrNumberConfigNotFoundException("No corr number generation configuration found for " + cnk + " nor for the default key of " + c);
		}
		
		String decodedString = getDecodedMaxId(co, cne);
		
		return decodedString;
	}

	public static String getCorrPrefix(CorrObject co ) throws CorrException, CorrNumberConfigNotFoundException
	{
		CorrNumberEntry cne = getCorrNumberEntry(co);
		String decodedString = getDecodedPrefix(co, cne);
		
		return decodedString;
	}

public static CorrNumberEntry getCorrNumberEntry(CorrObject co) throws CorrException, CorrNumberConfigNotFoundException {
		// get corr-prefix from the database configurations.
		HashMap<CorrNumberKey, CorrNumberEntry> configs = CorrNumberManager.getInstance().getCorrNumberCache().get(co.getBa().getSystemPrefix());
		
		CorrNumberKey cnk = getCorrNumberKey( co );
		
		if( null == configs )
			throw new CorrNumberConfigNotFoundException("No corr number generation configuration found for ba : " + co.getBa().getSystemPrefix());
		
		CorrNumberEntry cne = configs.get(cnk);
		
		if( null == cne )
		{	
			// find the default key i.e. with all types null.
			CorrNumberKey c = new CorrNumberKey(cnk.getSysPrefix(), null, null, null);
			cne = configs.get(c);
			if( null == cne )
				throw new CorrNumberConfigNotFoundException("No corr number generation configuration found for " + cnk + " nor for the default key of " + c);
		}
		
		return cne;
	}

	private static String getDecodedPrefix(CorrObject co, CorrNumberEntry cne) throws CorrException {
		Object [] fieldValues = getDecodedString(co,cne.getNumberFields());
		if( null == fieldValues )
			fieldValues = new Object[1];
		
		fieldValues[fieldValues.length-1] = "";
		
		return String.format(cne.getNumberFormat(), fieldValues);
	}

	private static Object[] getDecodedString(CorrObject co, String cslFields) throws CorrException 
	{
		if( null == cslFields )
			return null; 
		if( cslFields.trim().equals(""))
			return null;
		
		String[] fields = cslFields.split(",");
		Object[] values = new Object[fields.length + 1] ;
		
		int i = 0 ;
		
		for( String fieldName : fields )
		{
			fieldName = fieldName.trim();
			Field field = null;
			try {
				field = Field.lookupBySystemIdAndFieldName(co.getBa().getSystemId(), fieldName);
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( null == field )
				throw new CorrException("Field '" + fieldName + "' Not Found mentioned in configurations.");
			
			String fieldValue = co.getAsString(fieldName);
			switch( field.getDataTypeId() )
			{
				case DataType.TYPE :
					Type type = null;
				try {
					 type = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), fieldName, fieldValue);
				} catch (DatabaseException e1) {
					e1.printStackTrace();
				}
				
				if( type == null )
					throw new CorrException("Field with name : " + field.getDisplayName() + " was set to " + fieldValue + " which does not correspond to any type.");

				if( null == type.getDescription() )
					throw new CorrException("Type description was null for : " + type);
				
					values[i] = type.getDescription().trim();
					break;
				case DataType.STRING :
					values[i] = fieldValue;
					break;
				
				case DataType.INT :
					try
					{
						Integer v = Integer.parseInt(fieldValue);
						values[i] = v;
						break;
					}
					catch(Exception e)
					{
						e.printStackTrace();
						Utility.LOG.error(e);
						throw new CorrException("Exception occured while formating Integer : " + fieldValue);
					}
					
				case DataType.REAL:
					try
					{
						double d = Double.parseDouble(fieldValue);
						values[i] = d;
						break;
					}
					catch(Exception e)
					{
						e.printStackTrace();
						Utility.LOG.error(e);
						throw new CorrException("Exception occured while formating REAL : " + fieldValue);
					}
				case DataType.BOOLEAN: 
					try
					{
						boolean b = Boolean.parseBoolean(fieldValue);
						values[i] = b;
						break;
					}
					catch(Exception e)
					{
						e.printStackTrace();
						Utility.LOG.error(e);
						throw new CorrException("Exception occured while parsing BOOLEAN : " + fieldValue);
					}
					
				default : 
					throw new CorrException("Field '" + fieldName + "' is not supported for number system.");
			}
			i++;
		}
		
		return values;
	}

	private static CorrNumberKey getCorrNumberKey(CorrObject co) throws CorrException 
	{
		FieldNameEntry fne1 = FieldNameManager.lookupFieldNameEntry(co.getBa().getSystemPrefix(), GenericParams.NumType1);
		FieldNameEntry fne2 = FieldNameManager.lookupFieldNameEntry(co.getBa().getSystemPrefix(), GenericParams.NumType2);
		FieldNameEntry fne3 = FieldNameManager.lookupFieldNameEntry(co.getBa().getSystemPrefix(), GenericParams.NumType3);
		
		String type1 = null;
		String type2 = null; 
		String type3 = null;
		
		if( null != fne1 && null != fne1.getBaFieldName() )
			type1 = co.getAsString(fne1.getBaFieldName());
		
		if( null != fne2 && null != fne2.getBaFieldName() )
			type2 = co.getAsString(fne2.getBaFieldName());
		
		if( null != fne3 && null != fne3.getBaFieldName() )
			type3 = co.getAsString(fne3.getBaFieldName());
		
		return new CorrNumberKey(co.getBa().getSystemPrefix(), type1, type2, type3);
	}

	private static String getDecodedMaxId(CorrObject co, CorrNumberEntry cne) throws CorrException {
		Object [] fieldValues = getDecodedString(co,cne.getMaxIdFields());
		return String.format(cne.getMaxIdFormat(), fieldValues);
	}
}