/**
 * 
 */
package ksk;

import java.sql.Connection;
import java.util.Hashtable;

import ksk.ITransmittalRule;
import ksk.KSKUtils;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class RWISSITEPostTransmittalTargetBAUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": targer BA update post RWIS_SITE initiated transmittal.";
	}

	/* (non-Javadoc)
	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 2.2;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, 
	 * transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType,
			int businessAreaType, boolean isAddRequest) throws TBitsException {
		
		if (KSKUtils.isRWISSITE(dcrBA) && currentBA.getSystemPrefix().trim().equals(KSKUtils.RWIS)){

			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);	
			paramTable.put(Field.USER, "DC-RWIS-SITE");
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ERC_FROM_SITE)){
//				paramTable.put(Field.DUE_DATE, "");
				paramTable.put (Field.DESCRIPTION,"Comments received from SITE via DTN #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN_RWIS#" + transmittalRequest.getRequestId() + "]");
			}
		}
		
		if (KSKUtils.isRWISSITE(dcrBA) && currentBA.getSystemPrefix().trim().equals(KSKUtils.RWIS_CEC)){

			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);	
			paramTable.put(Field.USER, "DC-RWIS-SITE");
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ERC_FROM_SITE)){
//				paramTable.put(Field.DUE_DATE, "");
				paramTable.put (Field.DESCRIPTION,"Comments received from SITE via DTN #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN_RWIS#" + transmittalRequest.getRequestId() + "]");
			}
		}
	}
}