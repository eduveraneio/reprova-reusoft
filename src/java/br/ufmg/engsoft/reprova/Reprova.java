package br.ufmg.engsoft.reprova;

import br.ufmg.engsoft.reprova.database.Mongo;
import br.ufmg.engsoft.reprova.routes.Setup;

public class Reprova {
  public static void main(String[] args) {
    Setup.routes(new Mongo("reprova"));
  }
}

