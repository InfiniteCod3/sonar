/*
 *  Copyright (c) 2023, jones (https://jonesdev.xyz) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jones.sonar.common.command.subcommand.impl;

import jones.sonar.api.Sonar;
import jones.sonar.common.command.CommandInvocation;
import jones.sonar.common.command.subcommand.SubCommand;
import jones.sonar.common.command.subcommand.SubCommandInfo;

@SubCommandInfo(
        name = "verbose",
        description = "Enable and disable Sonar verbose",
        onlyPlayers = true
)
public final class VerboseCommand extends SubCommand {

    @Override
    public void execute(final CommandInvocation invocation) {
        final String verboseSubscriber = invocation.getExecutorName();

        if (Sonar.get().getActionBarVerbose().isSubscribed(verboseSubscriber)) {
            Sonar.get().getActionBarVerbose().unsubscribe(verboseSubscriber);
            invocation.getInvocationSender().sendMessage("§cUnsubscribed");
            return;
        }

        invocation.getInvocationSender().sendMessage("§aSubscribed");
        Sonar.get().getActionBarVerbose().subscribe(verboseSubscriber);
    }
}
