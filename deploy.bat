@call cd ..

@rem Downloading dependencies
@if not exist utils (
	@echo Cloning git repo for project utils
    @call git clone --branch 2.0-SNAPSHOT --single-branch https://github.com/Pierre-Emmanuel41/utils.git
) else ( 
	@call cd utils
	
	@echo Pulling latest changes for project utils
	@call git pull
	
	@call cd ..
)

@if not exist protocol (
	@echo Cloning git repo for project protocol
    @call git clone --branch 1.0-SNAPSHOT --single-branch https://github.com/Pierre-Emmanuel41/protocol.git
) else ( 
	@call cd protocol
	
	@echo Pulling latest changes for project protocol
	@call git pull
	
	@call cd ..
)

@if not exist voxy-common (
	@echo Cloning git repo for project voxy-common
    @call git clone --branch 1.0-SNAPSHOT --single-branch https://github.com/Pierre-Emmanuel41/voxy-common.git
) else ( 
	@call cd voxy-common
	
	@echo Pulling latest changes for project voxy-common
	@call git pull
	
	@call cd ..
)

@if not exist communication (
	@echo Cloning git repo for project communication
    @call git clone --branch 2.0-SNAPSHOT --single-branch https://github.com/Pierre-Emmanuel41/communication.git
) else (
	@call cd communication

	@echo Pulling latest changes for project protocol
	@call git pull

	@call cd ..
)

@if not exist messenger (
	@echo Cloning git repo for project messenger
    @call git clone --branch 2.0-SNAPSHOT --single-branch https://github.com/Pierre-Emmanuel41/messenger.git
) else (
	@call cd messenger

	@echo Pulling latest changes for project messenger
	@call git pull

	@call cd ..
)

@if not exist sound (
	@echo Cloning git repo for project sound
    @call git clone --branch 2.0-SNAPSHOT --single-branch https://github.com/Pierre-Emmanuel41/sound.git
) else (
	@call cd sound

	@echo Pulling latest changes for project sound
	@call git pull

	@call cd ..
)

@if not exist voxy-client (
	@echo Cloning git repo for project sound
    @call git clone --branch 1.0-SNAPSHOT --single-branch https://github.com/Pierre-Emmanuel41/voxy-client.git
) else (
	@call cd voxy-client

	@echo Pulling latest changes for project voxy-client
	@call git pull

	@call cd ..
)

@if not exist command-tree (
	@echo Cloning git repo for project command-tree
    @call git clone --branch 2.1-SNAPSHOT --single-branch https://github.com/Pierre-Emmanuel41/command-tree.git
) else (
	@call cd command-tree

	@echo Pulling latest changes for project command-tree
	@call git pull

	@call cd ..
)

@rem Building dependencies
@echo Building project utils
@call cd utils
@call mvn clean package install
@call cd ..

@echo Building project protocol
@call cd protocol
@call mvn clean package install
@call cd ..

@echo Building project voxy-common
@call cd voxy-common
@call mvn clean package install
@call cd ..

@echo Building project communication
@call cd communication
@call mvn clean package install
@call cd ..

@echo Building project messenger
@call cd messenger
@call mvn clean package install
@call cd ..

@echo Building project sound
@call cd sound
@call mvn clean package install
@call cd ..

@echo Building project voxy-client
@call cd voxy-client
@call mvn clean package install
@call cd ..

@echo Building project command-tree
@call cd command-tree
@call mvn clean package install
@call cd ..

@echo Building project voxy-client-command-line
@call cd voxy-client-command-line
@call mvn clean package install
@call cd ..