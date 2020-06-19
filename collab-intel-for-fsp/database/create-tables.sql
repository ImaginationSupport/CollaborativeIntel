SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `fsp` ;
CREATE SCHEMA IF NOT EXISTS `fsp` DEFAULT CHARACTER SET latin1 ;
USE `fsp` ;

-- -----------------------------------------------------
-- Table `fsp`.`featuretypes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`featuretypes` ;

CREATE TABLE IF NOT EXISTS `fsp`.`featuretypes` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `jar` VARCHAR(255) NOT NULL,
  `classname` VARCHAR(255) NOT NULL,
  `label` VARCHAR(255) NOT NULL,
  `description` VARCHAR(512) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `fsp`.`featuremaps`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`featuremaps` ;

CREATE TABLE IF NOT EXISTS `fsp`.`featuremaps` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `label` VARCHAR(255) NOT NULL,
  `featuretypeid` INT(11) NOT NULL,
  `units` VARCHAR(45) NULL,
  PRIMARY KEY (`id`),
  INDEX `featuretype_idx` (`featuretypeid` ASC),
  CONSTRAINT `featuretype`
    FOREIGN KEY (`featuretypeid`)
    REFERENCES `fsp`.`featuretypes` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 4
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `fsp`.`entitytypes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`entitytypes` ;

CREATE TABLE IF NOT EXISTS `fsp`.`entitytypes` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `label` VARCHAR(45) NOT NULL DEFAULT 'unknown entity type',
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`entities`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`entities` ;

CREATE TABLE IF NOT EXISTS `fsp`.`entities` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `typeid` INT NOT NULL,
  `label` VARCHAR(255) NOT NULL,
  `description` VARCHAR(512) NULL,
  PRIMARY KEY (`id`),
  INDEX `type_idx` (`typeid` ASC),
  CONSTRAINT `type`
    FOREIGN KEY (`typeid`)
    REFERENCES `fsp`.`entitytypes` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`states`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`states` ;

CREATE TABLE IF NOT EXISTS `fsp`.`states` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `start` DATETIME NOT NULL,
  `end` DATETIME NOT NULL,
  `p` DOUBLE NOT NULL,
  `label` VARCHAR(256) NOT NULL DEFAULT 'UNKNOWN',
  `active` TINYINT(1) NOT NULL DEFAULT true,
  `color` VARCHAR(45) NOT NULL DEFAULT '255,255,255',
  `description` VARCHAR(512) NOT NULL DEFAULT 'na',
  PRIMARY KEY (`id`),
  INDEX `dates_idx` (`start` ASC, `end` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`features`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`features` ;

CREATE TABLE IF NOT EXISTS `fsp`.`features` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `entityid` INT NOT NULL,
  `featuremapid` INT NOT NULL,
  `stateid` INT NOT NULL,
  `value` TEXT NOT NULL,
  `confidence` DOUBLE NOT NULL,
  INDEX `entity_idx` (`entityid` ASC),
  INDEX `feature_idx` (`featuremapid` ASC),
  INDEX `state_idx` (`stateid` ASC),
  PRIMARY KEY (`id`),
  CONSTRAINT `entity`
    FOREIGN KEY (`entityid`)
    REFERENCES `fsp`.`entities` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `feature`
    FOREIGN KEY (`featuremapid`)
    REFERENCES `fsp`.`featuremaps` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `state`
    FOREIGN KEY (`stateid`)
    REFERENCES `fsp`.`states` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`featureslots`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`featureslots` ;

CREATE TABLE IF NOT EXISTS `fsp`.`featureslots` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `entitytypeid` INT NOT NULL,
  `featuremapid` INT NOT NULL,
  INDEX `feature_idx` (`featuremapid` ASC),
  INDEX `entitytype_idx` (`entitytypeid` ASC),
  PRIMARY KEY (`id`),
  CONSTRAINT `featurefk`
    FOREIGN KEY (`featuremapid`)
    REFERENCES `fsp`.`featuremaps` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `entitytypefk`
    FOREIGN KEY (`entitytypeid`)
    REFERENCES `fsp`.`entitytypes` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`edges`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`edges` ;

CREATE TABLE IF NOT EXISTS `fsp`.`edges` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `label` VARCHAR(255) NOT NULL,
  `description` VARCHAR(512) NULL,
  `p` DOUBLE NOT NULL DEFAULT -1.0,
  `prev` INT NOT NULL,
  `next` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `next_idx` (`next` ASC),
  INDEX `prev_idx` (`prev` ASC),
  CONSTRAINT `prevfk`
    FOREIGN KEY (`prev`)
    REFERENCES `fsp`.`states` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `nextfm`
    FOREIGN KEY (`next`)
    REFERENCES `fsp`.`states` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`conditions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`conditions` ;

CREATE TABLE IF NOT EXISTS `fsp`.`conditions` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(45) NOT NULL,
  `label` VARCHAR(255) NOT NULL,
  `description` VARCHAR(512) NOT NULL,
  `prev` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`conditionoptions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`conditionoptions` ;

CREATE TABLE IF NOT EXISTS `fsp`.`conditionoptions` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `conditionid` INT NOT NULL,
  `edgeid` INT NOT NULL,
  `label` VARCHAR(255) NOT NULL,
  `p` DOUBLE NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `conditionid_idx` (`conditionid` ASC),
  INDEX `edgeid_idx` (`edgeid` ASC),
  CONSTRAINT `conditionid`
    FOREIGN KEY (`conditionid`)
    REFERENCES `fsp`.`conditions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `edgeid`
    FOREIGN KEY (`edgeid`)
    REFERENCES `fsp`.`edges` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`missions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`missions` ;

CREATE TABLE IF NOT EXISTS `fsp`.`missions` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `label` VARCHAR(255) NOT NULL,
  `description` VARCHAR(512) NOT NULL,
  `root` INT NOT NULL,
  `horizon` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `rootstateid_idx` (`root` ASC),
  CONSTRAINT `rootstateid`
    FOREIGN KEY (`root`)
    REFERENCES `fsp`.`states` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`projectortypes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`projectortypes` ;

CREATE TABLE IF NOT EXISTS `fsp`.`projectortypes` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `jar` VARCHAR(255) NOT NULL,
  `class` VARCHAR(255) NOT NULL,
  `label` VARCHAR(255) NOT NULL,
  `description` VARCHAR(512) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`projectors`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`projectors` ;

CREATE TABLE IF NOT EXISTS `fsp`.`projectors` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `label` VARCHAR(255) NOT NULL,
  `projectortypeid` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `typeid_idx` (`projectortypeid` ASC),
  CONSTRAINT `typeid`
    FOREIGN KEY (`projectortypeid`)
    REFERENCES `fsp`.`projectortypes` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`projectorparameterslot`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`projectorparameterslot` ;

CREATE TABLE IF NOT EXISTS `fsp`.`projectorparameterslot` (
  `id` INT NOT NULL,
  `label` VARCHAR(255) NOT NULL,
  `projectortypeid` INT NOT NULL,
  `featuretypeid` INT NOT NULL,
  `description` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `projectortypeid_idx` (`projectortypeid` ASC),
  INDEX `featuretypeid_idx` (`featuretypeid` ASC),
  CONSTRAINT `projectortypeid`
    FOREIGN KEY (`projectortypeid`)
    REFERENCES `fsp`.`projectortypes` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `featuretypeid`
    FOREIGN KEY (`featuretypeid`)
    REFERENCES `fsp`.`featuretypes` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`projectorparameter`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`projectorparameter` ;

CREATE TABLE IF NOT EXISTS `fsp`.`projectorparameter` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `projectorid` INT NOT NULL,
  `featuremapid` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `projectorid_idx` (`projectorid` ASC),
  INDEX `featuremapid_idx` (`featuremapid` ASC),
  CONSTRAINT `projectorid`
    FOREIGN KEY (`projectorid`)
    REFERENCES `fsp`.`projectors` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `featuremapid`
    FOREIGN KEY (`featuremapid`)
    REFERENCES `fsp`.`featuremaps` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`statecomments`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`statecomments` ;

CREATE TABLE IF NOT EXISTS `fsp`.`statecomments` (
  `id` INT NOT NULL,
  `stateid` INT NOT NULL,
  `description` VARCHAR(512) NULL,
  PRIMARY KEY (`id`),
  INDEX `stateid_idx` (`stateid` ASC),
  CONSTRAINT `stateid`
    FOREIGN KEY (`stateid`)
    REFERENCES `fsp`.`states` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`featurecomments`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`featurecomments` ;

CREATE TABLE IF NOT EXISTS `fsp`.`featurecomments` (
  `id` INT NOT NULL,
  `featureid` INT NOT NULL,
  `description` VARCHAR(512) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `featureid_idx` (`featureid` ASC),
  CONSTRAINT `featureid`
    FOREIGN KEY (`featureid`)
    REFERENCES `fsp`.`features` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`ciquestions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`ciquestions` ;

CREATE TABLE IF NOT EXISTS `fsp`.`ciquestions` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `questiontype` VARCHAR(45) NOT NULL,
  `label` VARCHAR(512) NOT NULL DEFAULT 'Unknown Question',
  `context` VARCHAR(2048) NOT NULL,
  `question` TEXT NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  `units` VARCHAR(45) NOT NULL DEFAULT 'unknown',
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`cicontextconditions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`cicontextconditions` ;

CREATE TABLE IF NOT EXISTS `fsp`.`cicontextconditions` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `questionid` INT NOT NULL,
  `conditionoptionid` INT NOT NULL,
  `html` VARCHAR(512) NULL,
  PRIMARY KEY (`id`),
  INDEX `questionid_idx` (`questionid` ASC),
  INDEX `ccco_idx` (`conditionoptionid` ASC),
  CONSTRAINT `qcc`
    FOREIGN KEY (`questionid`)
    REFERENCES `fsp`.`ciquestions` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `ccco`
    FOREIGN KEY (`conditionoptionid`)
    REFERENCES `fsp`.`conditionoptions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`cicontextfeatures`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`cicontextfeatures` ;

CREATE TABLE IF NOT EXISTS `fsp`.`cicontextfeatures` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `questionid` INT NOT NULL,
  `featureid` INT NOT NULL,
  `html` VARCHAR(512) NULL,
  PRIMARY KEY (`id`),
  INDEX `questionid_idx` (`questionid` ASC),
  INDEX `cff_idx` (`featureid` ASC),
  CONSTRAINT `qcf`
    FOREIGN KEY (`questionid`)
    REFERENCES `fsp`.`ciquestions` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `cff`
    FOREIGN KEY (`featureid`)
    REFERENCES `fsp`.`features` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`citargetfeatures`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`citargetfeatures` ;

CREATE TABLE IF NOT EXISTS `fsp`.`citargetfeatures` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `questionid` INT NOT NULL,
  `featureid` INT NOT NULL,
  `updated` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `questionid_idx` (`questionid` ASC),
  INDEX `tff_idx` (`featureid` ASC),
  CONSTRAINT `qtf`
    FOREIGN KEY (`questionid`)
    REFERENCES `fsp`.`ciquestions` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `tff`
    FOREIGN KEY (`featureid`)
    REFERENCES `fsp`.`features` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`cicrowdinput`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`cicrowdinput` ;

CREATE TABLE IF NOT EXISTS `fsp`.`cicrowdinput` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `questionid` INT NOT NULL,
  `site` VARCHAR(512) NOT NULL DEFAULT 'TestOnly',
  `user` INT NOT NULL DEFAULT 0,
  `date` DATETIME NOT NULL,
  `value` TEXT NOT NULL,
  `confidence` DOUBLE NOT NULL DEFAULT -1.0,
  PRIMARY KEY (`id`),
  INDEX `questionid_idx` (`questionid` ASC),
  CONSTRAINT `qci`
    FOREIGN KEY (`questionid`)
    REFERENCES `fsp`.`ciquestions` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`cicrowdexplains`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`cicrowdexplains` ;

CREATE TABLE IF NOT EXISTS `fsp`.`cicrowdexplains` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `questionid` INT NOT NULL,
  `eventlabel` VARCHAR(255) NOT NULL,
  `eventdesc` VARCHAR(512) NOT NULL,
  `option1` VARCHAR(255) NOT NULL,
  `option2` VARCHAR(255) NOT NULL,
  `votes` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  INDEX `question_idx` (`questionid` ASC),
  CONSTRAINT `question`
    FOREIGN KEY (`questionid`)
    REFERENCES `fsp`.`ciquestions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `fsp`.`ciconditions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fsp`.`ciconditions` ;

CREATE TABLE IF NOT EXISTS `fsp`.`ciconditions` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `questionid` INT NOT NULL,
  `featurequestionid` INT NOT NULL,
  `question` VARCHAR(256) NOT NULL,
  `option1` VARCHAR(256) NOT NULL,
  `option2` VARCHAR(256) NOT NULL,
  `option1value` VARCHAR(256) NOT NULL,
  `option2value` VARCHAR(256) NOT NULL,
  `option1conf` DOUBLE NOT NULL DEFAULT -1.0,
  `option2conf` DOUBLE NOT NULL DEFAULT -1.0,
  `option1edgeid` INT NOT NULL,
  `option2edgeid` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `questionid_idx` (`questionid` ASC),
  CONSTRAINT `questionid`
    FOREIGN KEY (`questionid`)
    REFERENCES `fsp`.`ciquestions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
