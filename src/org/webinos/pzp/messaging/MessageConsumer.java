/**
 * 
 */
package org.webinos.pzp.messaging;

/**
 * @author johl
 *
 */
public interface MessageConsumer {

	public boolean acceptsMessage(WebinosMessage msg);

	public void consume(WebinosMessage msg);

}
