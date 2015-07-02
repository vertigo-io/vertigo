/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.commons.event;

import io.vertigo.lang.Component;

import java.io.Serializable;

/**
 * Inter-components events manager.
 * Producer/Consumer on channel for communication between components.
 * When registering to channel, Listeners are configured to listen locally only.
 *
 * Example :
 * A cache component should listen : a modification in one app should flush cache all over the system : it's cache component responsibility to do this
 * An audit component should listen to do the audit log
 *
 * @author pchretien, npiedeloup
 */
public interface EventManager extends Component {

	/**
	 * Fire an event on a channel.
	 * @param <P> Payload's type
	 * @param channel ChannelName to send event to
	 * @param payload event's payload
	 */
	<P extends Serializable> void fire(EventChannel<P> channel, P payload);

	/**
	 * Register a new listener for this channel.
	* @param <P> Payload's type
	 * @param channel ChannelName to listen
	 * @param eventsListener EventsListener
	 */
	<P extends Serializable> void register(EventChannel<P> channel, EventListener<P> eventsListener);

}
