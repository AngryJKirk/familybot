# Architecture of Familybot

### 1. Basics

#### 1.1 Router

The main entry point of all bot is a so-called `Router`.

It accepts `Update`, which is being proceeded by some executor if there is one can be applied.

#### 1.2 Diagram of Router logic

The diagram below shows the Router's behaviour.
![diagram](https://i.ibb.co/VtqzFW6/excalidraw-2020330164515.png)

For a better understanding of the diagram, it is necessary to talk about executors.

### 1.3 Executor interfaces

There are many implementations of executors, all of them will be described below. All of them are made to be responsible
for some certain function of the bot.

#### 1.3.1 Basic executor

Basically, Executor is an interface, which is presented by 3 methods listed below.

```kotlin
interface Executor {
    fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit

    fun canExecute(context: ExecutorContext): Boolean

    fun priority(context: ExecutorContext): Priority
}
``` 

##### Executor.priority()

That method decides, which priority should executor have.

There are several priority levels:

1) `HIGH`
2) `MEDIUM`
3) `LOW`
4) `VERY_LOW`
5) `RANDOM`

Frankly, this can be considered as an architectural mistake: it would be better to use just numbers. After all, from the
beginning of the project there were only 3 levels, and later others were added. It means that it is possible to add some
other levels in the future, and it would be odd at least to have a level with a name like `VERY_VERY_LOW`.

The last level in the list above (called `RANDOM`) doesn't take part in choosing executors. Executors with levels like
that are usually chosen by random (what a surprise!).

##### Executor.canExecute()

This method is being called after sorting based on a priority of an executor. It decides, basically, will this executor
take responsibility to process an Update or not.

Despite other methods, this one only gets a Message instead of an Update due to some historical events which I don't
remember well.

##### Executor.execute()

That one is invoked only after passing `canExecute` method with a proper result. It returns a function, which should be
applied using a client.

This is a weak part of this project's architecture. That is a real pain when you have to do tests. It is a part of a big
plan for refactoring of the project.

#### 1.3.2 Command executor

Command executor is made to create commands. It handles such things like `priority()` calls or
`canExecute()` calls. They are already implemented, first one is always `MEDIUM`, the second one can decide if the
Message contains the command which is linked to this executor.

But, Command executor obligates to implement methods like `command()`, which should provide an enum constant containing
the command that belongs to the executor.

#### 1.3.3 ContinuousConversation executor

Executor, which is created to handle replies which were addressed to other executors. As a rule, there is always another
executor which is related to `ContinuousExecutor`.

It has an implemented method as well, and they are similar to command executor, the difference is `getDialogMessage()`
method, which determines the phrase that has been written by the bot.

For example, there can be a dialog like that:
> <...>
>
>User: /bet
>
> Bot: Приготовь своё очко, ща разыграем его в кругу своих друзей,
> только скажи какую ставку делаешь?
>
>User replies to Bot: 3
>
> Bot: Кручу верчу, наебать хочу
>
> <...>

Continuous executor's dialog message should
be `Приготовь своё очко, ща разыграем его в кругу своих друзей, только скажи какую ставку делаешь?`
to be able to process the message.

#### 1.3.4 Private message executor

There is some functionality that is available when you use private messages instead of when you add the Bot to a group.

That interface doesn't have any additional logic comparing to the basic Executor interface. It is just a marker which is
created to separate executors when the incoming message is not from a group.

#### 1.3.5 Configurable

This is not an executor interface implementation, but it is an important part of the system. The interface adds a method
called `getFunctionId()` which helps to understand, what functionality represents some executor.

For example, there are `Talking` functionality, which relates to a few executors. All the executors
implement `Configurable` interface, so they can be disabled as one. More information about disabling functions is TBD.

#### 1.4 Executor implementations

Currently, there are 37 already implemented executors. There are 4 main groups of executors:

1) Command
2) Event-based
3) Continuous
4) PM

##### 1.4.1 Command

That executors implement `CommandExecutor` interface. They are made to execute direct commands from users to the bot.

##### 1.4.1.1 Answer executor

 Command   | Interfaces | FunctionId 
-----------|------------|------------
 `/asnwer` | Command    | -          

Its behaviour is quite simple, it accepts message like:

```
/answer 1 или 2 или 3
``` 

The bot splits the message by `или` and replies with a randomly chosen option.

##### 1.4.1.2 Me executor

 Command | Interfaces | FunctionId 
---------|------------|------------
 `/me`   | Command    | -          

It counts all the stats about a user, such as the number of messages in the current chat, or the number of commands
which were given to the bot from the user.

##### 1.4.1.2 Pidor executor

 Command  | Interfaces            | FunctionId 
----------|-----------------------|------------
 `/pidor` | Command, Configurable | PIDOR      

The most well-known functionality of the bot is a pidor choosing.

This executor picks the list of users and chose one of them. After that, the bot name that guy as a pidor of a day.

It can be rolled twice if a chat has more than 50 active members.

Also, it invokes pidor competition service, which is active only on the last day of a month.

The mission of pidor competition service is to leave zero uncertainty regarding the pidor of a month.

That is made to avoid such a situation where there are two leaders of the month in the score table.

Pidor executor is also a place, where all the users in a chat are being checked using Telegram Bot API
call `GetChatMember()`. It checks whenever a user leaves chat without any message about that from Telegram.  
It happens when a chat has more than ~50 members.

TBD.
