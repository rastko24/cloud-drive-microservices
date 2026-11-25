db = db.getSiblingDB("notifikacije_db");

db.notifikacije.insertMany([
  {
    email: "petrovicfilip112@gmail.com",
    poruka: "Dobrodosao na nalog - verifikacija je uspesna.",
    vremeKreiranja: new Date(Date.now() - 10800000)
  },
  {
    email: "petrovicfilip112@gmail.com",
    poruka: "Kreiran je novi folder u drive strukturi.",
    vremeKreiranja: new Date(Date.now() - 1800000)
  },
  {
    email: "petrovicfilip112@gmail.com",
    poruka: "Nova notifikacija - sve radi kako treba.",
    vremeKreiranja: new Date()
  },
  {
    email: "rale123@gmail.com",
    poruka: "Ova notifikacija pripada drugom korisniku (Rastku).",
    vremeKreiranja: new Date()
  }
]);