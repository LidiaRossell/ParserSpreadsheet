package controller.parser;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.*;

import controller.parser.Token.typeToken;

import java.util.Arrays;
import java.util.List;
import java.lang.Math;

public class parser {
    /**
     * Input que se tiene que traducir
     */
    public String input;
    /**
     * PosiciÃ³n dentro de input por donde estamos traduciendo
     */
    private Integer pos = 0;

    private boolean exception = false;

    /**
     * Diferentes tipos de celda que existen
     */
    enum tipoCelda {numerica, textual, fecha, monetaria}

    ;

    /**
     * Aumenta la posiciÃ³n actual en uno
     *
     * @return carÃ¡cter que había en la posición de input antes de aumentar pos
     */
    private char next() {
        char ch = input.charAt(pos++);
        return ch;
    }

    /**
     * Consulta el carÃ¡cter de input en la posiciÃ³n pos
     *
     * @return carÃ¡cter consultado
     */
    private char peek() {
        return input.charAt(pos);
    }

    /**
     * Comprueba si pos ha llegado al final
     *
     * @return indica si pos ha llegado al final
     */
    private boolean eof() {
        return (input.length() <= pos);
    }

    /**
     * EnvÃ­a un mensaje de error con la posiciÃ³n dentro de input
     *
     * @param msg mensaje que acompaÃ±a al error
     */
    private void croak(String msg) {
        System.out.println(msg + " pos: " + pos.toString());
        exception = true;
    }

    /**
     * Salta todos los espacios en blanco que hay a partir de pos en input
     */
    private void readWhiteSpaces() {
        while (!eof() && peek() == ' ') next();
    }

    /**
     * Dado la posiciÃ³n de un inicio de parÃ©ntesis en input, busca el final
     * de dicho parÃ©ntesis
     *
     * @param j PosiciÃ³n de '('
     * @return PosiciÃ³n del correspondiente ')'
     */
    private Integer skipParenthesis(Integer j) {
        ++j;
        while (j < input.length() && input.charAt((j)) != ')') {
            if (input.charAt(j) == '(') j = skipParenthesis(j);
            ++j;
        }
        if (j == input.length()) croak("Expresion no valida. Falta cerrar parentesis");
        return j;
    }

    /**
     * Dado el sÃ­mbolo de una operaciÃ³n (+,-,*,/), devuelve un TokenOperacion
     * que representa dicha operaciÃ³n
     *
     * @param c sÃ­mbolo de la operaciÃ³n
     * @return TokenOperacion que representa la operaciÃ³n pasada
     */
    private TokenOperacion traducirSimb(char c) {
        TokenOperacion result = new TokenOperacion();
        result.type = Token.typeToken.operacion;
        switch (c) {
            case '+':
                result.operation = TokenOperacion.opName.add;
                break;
            case '-':
                result.operation = TokenOperacion.opName.subs;
                break;
            case '*':
                result.operation = TokenOperacion.opName.mult;
                break;
            case '/':
                result.operation = TokenOperacion.opName.div;
                break;
        }
        result.initializeOp();
        return result;
    }

    /**
     * Lee una expresiÃ³n del tipo operaciÃ³n, nÃºmero o string de input
     *
     * @return Devuelve el OpTree de la expresiÃ³n leÃ­da
     */
    private OpTree readExp() {
        readWhiteSpaces();
        OpTree result = new OpTree();
        if (eof()) {
            result.node = readLitString();
            result.hijos = new ArrayList<ArrayList<OpTree>>();
        } else if ((peek() >= 'a' && peek() <= 'z') || (peek() >= 'A' && peek() <= 'Z')) {
            result = readOp();
        } else if ((peek() >= '0' && peek() <= '9') || peek() == '-') {
            result = readNum();
        } else if (peek() == '\"') {
            next();
            result = readString();
        } else {
            croak("Expresion invalida. Se espera una operacion, un numero, un string o un bloque");
        }
        readWhiteSpaces();

        return result;
    }

    /**
     * Lee una operaciÃ³n con sus argumentos, o un bloque si se trata de ese tipo de Token
     *
     * @return InformaciÃ³n leÃ­da, ya sea una operaciÃ³n o un bloque
     */
    private OpTree readOp() {
        OpTree result = new OpTree();
        result.hijos = new ArrayList<ArrayList<OpTree>>();
        result.node = readOpName();
        if (exception) return result;
        if (result.node.type == Token.typeToken.operacion) {
            if (!String.valueOf(next()).equals("(")) {
                croak("Expresion no valida. Se espera '('");
                return result;
            }
            for (Integer i = 0; i < ((TokenOperacion) result.node).numArguments; ++i) {
                result.hijos.add(readArguments());
                if (i.equals(((TokenOperacion) result.node).numArguments - 1)) {
                    if (eof() || next() != ')') croak("Expresion no valida. Se espera ')'");
                } else if (eof() || !(String.valueOf(next())).equals(";")) croak("Expresion no valida. Se espera ';'");
                if (exception) return result;
            }
        }

        return result;
    }

    /**
     * Lee un nombre de operaciÃ³n y devuelve un Token que devuelva dicha operaciÃ³n o,
     * si era un bloque, devuelve un Token con dicho bloque
     *
     * @return Token con la informaciÃ³n leÃ­da. Puede ser una operaciÃ³n o un bloque
     */
    private Token readOpName() {
        TokenOperacion result = new TokenOperacion();

        Map<String, TokenOperacion.opName> mapTranslateOps;
        mapTranslateOps = initializeTranslationMap();

        String nombreOp = "";
        while (!eof() && ((peek() >= 'a' && peek() <= 'z') || (peek() >= 'A' && peek() <= 'Z'))) {
            nombreOp += Character.toLowerCase(next());
        }
        if (!eof() && peek() >= '0' && peek() <= '9') {
            return readBloque(nombreOp);
        }
        if (!mapTranslateOps.containsKey(nombreOp)) {
            croak("Expresion no valida. Nombre de operacion no es correcto");
            return result;
        }
        result.type = Token.typeToken.operacion;
        result.operation = mapTranslateOps.get(nombreOp);
        result.initializeOp();
        return result;
    }

    /**
     * Inicializa un map con las posibles traducciones de operaciones que el parser recibe
     * a una operaciÃ³n opName
     *
     * @return mapa con las traducciones
     */
    private Map<String, TokenOperacion.opName> initializeTranslationMap() {
        Map<String, TokenOperacion.opName> result = Stream.of(new Object[][]{
                {"round", TokenOperacion.opName.round},
                {"redondear", TokenOperacion.opName.round},
                {"floor", TokenOperacion.opName.floor},
                {"truncar", TokenOperacion.opName.floor},
                {"abs", TokenOperacion.opName.abs},
                {"valorabsoluto", TokenOperacion.opName.abs},
                {"sqrt", TokenOperacion.opName.sqrt},
                {"raizcuadrada", TokenOperacion.opName.sqrt},
                {"add", TokenOperacion.opName.add},
                {"suma", TokenOperacion.opName.add},
                {"subs", TokenOperacion.opName.subs},
                {"resta", TokenOperacion.opName.subs},
                {"mult", TokenOperacion.opName.mult},
                {"multiplicacion", TokenOperacion.opName.mult},
                {"div", TokenOperacion.opName.div},
                {"division", TokenOperacion.opName.div},
                {"mod", TokenOperacion.opName.mod},
                {"modulo", TokenOperacion.opName.mod},
                {"pow", TokenOperacion.opName.pow},
                {"potencia", TokenOperacion.opName.pow},
                {"inc", TokenOperacion.opName.inc},
                {"incrementar", TokenOperacion.opName.inc},
                {"dec", TokenOperacion.opName.dec},
                {"decrementar", TokenOperacion.opName.dec},
                {"bintodec", TokenOperacion.opName.binToDec},
                {"binarioadecimal", TokenOperacion.opName.binToDec},
                {"hextodec", TokenOperacion.opName.hexToDec},
                {"hexadecimaladecimal", TokenOperacion.opName.hexToDec},
                {"octtodec", TokenOperacion.opName.octToDec},
                {"octaladecimal", TokenOperacion.opName.octToDec},
                {"tobin", TokenOperacion.opName.toBin},
                {"binario", TokenOperacion.opName.toBin},
                {"tohex", TokenOperacion.opName.toHex},
                {"hexadecimal", TokenOperacion.opName.toHex},
                {"tooct", TokenOperacion.opName.toOct},
                {"octal", TokenOperacion.opName.toOct},
                {"convertir", TokenOperacion.opName.convertir},
                {"media", TokenOperacion.opName.media},
                {"mediana", TokenOperacion.opName.mediana},
                {"varianza", TokenOperacion.opName.varianza},
                {"covarianza", TokenOperacion.opName.covarianza},
                {"desviacion", TokenOperacion.opName.desviacion},
                {"desviacionestandar", TokenOperacion.opName.desviacion},
                {"pearson", TokenOperacion.opName.pearson},
                {"coeficientecorrelacionpearson", TokenOperacion.opName.pearson},
                {"extractday", TokenOperacion.opName.extractDay},
                {"extraerdia", TokenOperacion.opName.extractDay},
                {"extractmonth", TokenOperacion.opName.extractMonth},
                {"extraermes", TokenOperacion.opName.extractMonth},
                {"extractyear", TokenOperacion.opName.extractYear},
                {"extraerano", TokenOperacion.opName.extractYear},
                {"weekday", TokenOperacion.opName.weekDay},
                {"diasemana", TokenOperacion.opName.weekDay},
                {"newdate", TokenOperacion.opName.newDate},
                {"calcularfecha", TokenOperacion.opName.newDate},
                {"replace", TokenOperacion.opName.replace},
                {"reemplazar", TokenOperacion.opName.replace},
                {"size", TokenOperacion.opName.size},
                {"tamaÃ±o", TokenOperacion.opName.size}
        }).collect(Collectors.toMap(data -> (String) data[0], data -> (TokenOperacion.opName) data[1]));

        return result;
    }

    /**
     * Lee un conjunto de celdas (puede contener solo 1 celda) y devuelve el Token
     * del bloque que conforman
     *
     * @param s columna de la primera celda
     * @return Token con la informaciÃ³n del bloque leÃ­do
     */
    private TokenBloque readBloque(String s) {
        TokenBloque result = new TokenBloque();
        Integer x1, y1, x2, y2;
        y1 = columnToInt(s);
        x1 = 0;
        while (!eof() && peek() >= '0' && peek() <= '9') {
            x1 *= 10;
            x1 += Character.getNumericValue(next());
        }
        if (!eof() && peek() == ':') {
            next();
            y2 = 0;
            while (!eof() && ((peek() >= 'a' && peek() <= 'z') || (peek() >= 'A' && peek() <= 'Z'))) {
                y2 *= ('z' - 'a' + 1);
                y2 += (Character.toLowerCase(next()) - 'a' + 1);
            }
            if (eof() || peek() < '0' || peek() > '9') {
                croak("Expresion no valida. Se esperaba una celda");
                return result;
            }
            x2 = 0;
            while (!eof() && peek() >= '0' && peek() <= '9') {
                x2 *= 10;
                x2 += Character.getNumericValue(next());
            }
        } else {
            x2 = x1;
            y2 = y1;
        }
        if (x1 == 0 || x2 == 0) croak("Expresion no valida. Fila de celda no puede ser 0");
        result.type = Token.typeToken.bloque;
        result.listaCeldas = getCeldas(x1, y1, x2, y2);
        return result;
    }

    /**
     * Dado un identificador de columna, lo transforma al nÃºmero de columna que corresponde
     *
     * @param s string que identifica la columna
     * @return nÃºmero de columna correspondiente
     */
    private Integer columnToInt(String s) {
        Integer result = 0;
        for (Integer i = 0; i < s.length(); ++i) {
            result *= ('z' - 'a' + 1);
            result += (s.charAt(i) - 'a' + 1);
        }
        return result;
    }

    /**
     * Dadas dos celdas, devuelve todas las celdas del rectÃ¡ngulo de celdas que delimitan
     *
     * @param x1 fila de la primera celda
     * @param y1 columna de la primera celda
     * @param x2 fila de la segunda celda
     * @param y2 columna de la segunda celda
     * @return conjunto de celdas que pertenecen al rectÃ¡ngulo delimitado por las dos celdas pasadas
     */
    private ArrayList<ArrayList<Integer>> getCeldas(Integer x1, Integer y1, Integer x2, Integer y2) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
        for (int i = Math.min(x1, x2); i <= Math.max(x1, x2); ++i) {
            for (int j = Math.min(y1, y2); j <= Math.max(y1, y2); ++j) {
                ArrayList<Integer> aux = new ArrayList<Integer>();
                aux.add(i);
                aux.add(j);
                result.add(aux);
            }
        }
        return result;
    }

    /**
     * Lee los argumentos separados por comas de una operaciÃ³n
     *
     * @return lista de argumentos, expresados en OpTree, leÃ­dos
     */
    private ArrayList<OpTree> readArguments() {
        ArrayList<OpTree> result = new ArrayList<OpTree>();
        result.add(readExp());
        if (exception) return result;
        readWhiteSpaces();
        while (!eof() && String.valueOf(peek()).equals(",")) {
            next();
            readWhiteSpaces();
            result.add(readExp());
            if (exception) return result;
            readWhiteSpaces();
        }
        return result;
    }

    /**
     * Lee un nÃºmero y devuelve un OpTree que lo representa
     *
     * @return OpTree que representa el nÃºmero leÃ­do
     */
    private OpTree readNum() {
        OpTree result = new OpTree();
        result.hijos = new ArrayList<ArrayList<OpTree>>();
        TokenLNumerico nodo = new TokenLNumerico();
        nodo.type = Token.typeToken.literal;
        nodo.LType = TokenLiteral.literalType.numerico;
        boolean negative = false;
        if (peek() == '-') {
            next();
            negative = true;
        }
        if (eof()) {
            croak("Expresion no valida. Se espera un numero, una operacion, un string o un conjunto de celdas");
            return result;
        }
        nodo.contenido = 0;
        while (!eof() && peek() >= '0' && peek() <= '9') {
            nodo.contenido *= 10;
            nodo.contenido += (next() - '0');
        }
        if (!eof() && String.valueOf(peek()).equals(".")) {
            Integer decimals = 0;
            next();
            while (!eof() && peek() >= '0' && peek() <= '9') {
                ++decimals;
                nodo.contenido *= 10;
                nodo.contenido += (next() - '0');
            }
            nodo.contenido /= Math.pow((double) 10, (double) decimals);
        }
        if (!eof() && !" ,;)".contains(String.valueOf(peek()))) {
            croak("Expresion no valida. Numero esperado");
            return result;
        }
        if (negative) nodo.contenido *= -1;
        result.node = nodo;
        return result;
    }

    /**
     * Lee un string y devuelve su OpTree correspondiente
     *
     * @return OpTree del string leÃ­do
     */
    private OpTree readString() {
        OpTree result = new OpTree();
        result.hijos = new ArrayList<ArrayList<OpTree>>();
        TokenLTextual nodo = new TokenLTextual();
        nodo.type = Token.typeToken.literal;
        nodo.LType = TokenLiteral.literalType.textual;
        nodo.contenido = "";
        while (!eof() && ((!String.valueOf(peek()).equals("\"")) || (nodo.contenido.length() >= 1 && String.valueOf(nodo.contenido.charAt(nodo.contenido.length() - 1)).equals("\\")))) {
            if (String.valueOf(peek()).equals("\"")) {
                nodo.contenido.substring(0, nodo.contenido.length() - 2);
                nodo.contenido += '\"';
                next();
            }
            nodo.contenido += next();
        }
        if (eof()) {
            croak("Expresion no valida. Se deben cerrar las comillas del string");
        } else next();
        return result;
    }

    /**
     * Lee un string, nÃºmero o fecha y devuelve su OpTree correspondiente
     *
     * @return OpTree correspondiente a la informaciÃ³n leÃ­da
     */
    private OpTree readLiteral() {
        OpTree result = new OpTree();
        result.hijos = new ArrayList<ArrayList<OpTree>>();
        result.node = new TokenLiteral();
        result.node.type = Token.typeToken.literal;
        readWhiteSpaces();
        if (!eof() && ((peek() >= '0' && peek() <= '9') || peek() == '-')) {
            result.node = readLitNum();
        } else result.node = readLitString();

        return result;
    }

    /**
     * Lee un nÃºmero y devuelve su OpTree correspondiente
     *
     * @return OpTree correspondiente a la informaciÃ³n leÃ­da
     */
    private TokenLiteral readLitNum() {
        double contenido = 0;
        boolean negative = false;
        if (peek() == '-') {
            next();
            negative = true;
        }
        while (!eof() && peek() >= '0' && peek() <= '9') {
            contenido *= 10;
            contenido += (next() - '0');
        }

        if (!negative && !eof() && (String.valueOf(peek()).equals("/") || String.valueOf(peek()).equals("-"))) {
            return readLitDate((Integer) (int) contenido);
        }

        if (!eof() && String.valueOf(peek()).equals(".")) {
            Integer decimals = 0;
            next();
            while (!eof() && peek() >= '0' && peek() <= '9') {
                ++decimals;
                contenido *= 10;
                contenido += (next() - '0');
            }
            contenido /= Math.pow((double) 10, (double) decimals);
        }
        readWhiteSpaces();

        if (negative) contenido *= -1;
        if (!eof()) return readLitMoneda(contenido);

        TokenLNumerico result = new TokenLNumerico();
        result.type = Token.typeToken.literal;
        result.LType = TokenLiteral.literalType.numerico;
        result.contenido = contenido;
        return result;
    }

    /**
     * Lee una fecha y devuelve su Token correspondiente. Si no es una fecha,
     * devuelve un Token de string
     *
     * @param n nÃºmero del dÃ­a de la fecha
     * @return Token que representa la fecha leÃ­da, y si no lo es, representa un string leÃ­do
     */
    private TokenLiteral readLitDate(Integer n) {
        TokenLFecha result = new TokenLFecha();
        result.type = Token.typeToken.literal;
        result.LType = TokenLiteral.literalType.fecha;
        result.day = n;
        result.month = 0;
        result.year = 0;
        char divider = next();
        while (!eof() && peek() >= '0' && peek() <= '9') {
            result.month *= 10;
            result.month += (next() - '0');
        }
        if (eof() || next() != divider) {
            return readLitString();
        }
        while (!eof() && peek() >= '0' && peek() <= '9') {
            result.year *= 10;
            result.year += (next() - '0');
        }
        readWhiteSpaces();
        if (!eof() || !fechaCorrecta(result.day, result.month, result.year)) return readLitString();
        return result;
    }

    /**
     * Lee el string de input
     *
     * @return Token de string que tiene como contenido el input
     */
    private TokenLTextual readLitString() {
        TokenLTextual result = new TokenLTextual();
        result.type = Token.typeToken.literal;
        result.LType = TokenLiteral.literalType.textual;
        result.contenido = input;
        return result;
    }

    /**
     * Determina si una fecha es vÃ¡lida
     *
     * @param day   dÃ­a de la fecha
     * @param month mes de la fecha
     * @param year  aÃ±o de la fecha
     * @return dice si la fecha dada es vÃ¡lida
     */
    private boolean fechaCorrecta(Integer day, Integer month, Integer year) {
        if (month > 12 || month < 1) return false;
        if (day < 1 || day > 31) return false;
        if (month == 2) {
            if (day > 29) return false;
            if (year % 4 != 0 && day == 29) return false;
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            if (day == 30) return false;
        }
        return true;
    }

    /**
     * Lee un literal que representa una moneda y devuelve su Token correspondiente.
     * Si no es una moneda, devuelve un Token de string
     *
     * @param cont valor nÃºmerico del Token
     * @return Token que contiene la informaciÃ³n leÃ­da
     */
    private TokenLiteral readLitMoneda(double cont) {
        Map<String, TipoMoneda> mapMonedas;
        mapMonedas = initializeCoinMap();

        String nombreMoneda = "";
        while (!eof() && peek() != ' ') {
            nombreMoneda += Character.toUpperCase(next());
        }

        if (!mapMonedas.containsKey(nombreMoneda)) return readLitString();
        TokenLMonetario result = new TokenLMonetario();
        result.type = Token.typeToken.literal;
        result.LType = TokenLiteral.literalType.numerico;
        result.contenido = cont;
        result.moneda = mapMonedas.get(nombreMoneda);
        return result;
    }

    /**
     * Inicializa un mapa con las diferentes codificaciones de los tipos de moneda
     * que puede haber en input
     *
     * @return mapa con las relaciones de codificaciones de monedas con el tipo que les corresponde
     */
    private Map<String, TipoMoneda> initializeCoinMap() {
        Map<String, TipoMoneda> result = Stream.of(new Object[][]{
                {"$", TipoMoneda.USD},
                {"€", TipoMoneda.EUR},
                {"¥", TipoMoneda.JPY},
                {"£", TipoMoneda.GBP},
                {"A$", TipoMoneda.AUD},
                {"C$", TipoMoneda.CAD},
                {"HK$", TipoMoneda.HDK},
                {"R$", TipoMoneda.BRL},
                {"₽", TipoMoneda.RUB},
                {"元", TipoMoneda.CNY},
                {"EUR", TipoMoneda.EUR},
                {"USD", TipoMoneda.USD},
                {"GBP", TipoMoneda.GBP},
                {"CHF", TipoMoneda.CHF},
                {"JPY", TipoMoneda.JPY},
                {"HDK", TipoMoneda.HDK},
                {"CAD", TipoMoneda.CAD},
                {"CNY", TipoMoneda.CNY},
                {"AUD", TipoMoneda.AUD},
                {"BRL", TipoMoneda.BRL},
                {"RUB", TipoMoneda.RUB},
                {"MXN", TipoMoneda.MXN},
                {"BTC", TipoMoneda.BTC},
                {"ETH", TipoMoneda.ETH},
                {"USDT", TipoMoneda.USDT},
                {"BNB", TipoMoneda.BNB},
                {"XRP", TipoMoneda.XRP},
                {"ADA", TipoMoneda.ADA},
                {"SOL", TipoMoneda.SOL},
                {"LUNA", TipoMoneda.LUNA},
                {"DOGE", TipoMoneda.DOGE}
        }).collect(Collectors.toMap(data -> (String) data[0], data -> (TipoMoneda) data[1]));
        return result;
    }

    /**
     * Lee un conjunto de celdas (puede contener solo 1 celda) y devuelve el Token
     * del bloque que conforman
     *
     * @return Token con la informaciÃ³n del bloque leÃ­do
     */
    private ArrayList<ArrayList<Integer>> readBloque() {
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
        Integer x1, y1, x2, y2;
        if (eof() || Character.toLowerCase(peek()) < 'a' || Character.toLowerCase(peek()) > 'z')
            croak("ExpresiÃ³n no vÃ¡lida. Se esperaba leer un bloque");
        y1 = 0;
        while (!eof() && ((peek() >= 'a' && peek() <= 'z') || (peek() >= 'A' && peek() <= 'Z'))) {
            y1 *= ('z' - 'a' + 1);
            y1 += (Character.toLowerCase(next()) - 'a' + 1);
        }
        if (eof() || peek() < '0' || peek() > '9') {
            croak("Expresion no valida. Se esperaba un bloque");
            return result;
        }
        x1 = 0;
        while (!eof() && peek() >= '0' && peek() <= '9') {
            x1 *= 10;
            x1 += Character.getNumericValue(next());
        }
        if (!eof() && peek() == ':') {
            next();
            y2 = 0;
            while (!eof() && ((peek() >= 'a' && peek() <= 'z') || (peek() >= 'A' && peek() <= 'Z'))) {
                y2 *= ('z' - 'a' + 1);
                y2 += (Character.toLowerCase(next()) - 'a' + 1);
            }
            if (eof() || peek() < '0' || peek() > '9') {
                croak("Expresion no valida");
                return result;
            }
            x2 = 0;
            while (!eof() && peek() >= '0' && peek() <= '9') {
                x2 *= 10;
                x2 += Character.getNumericValue(next());
            }
        } else {
            x2 = x1;
            y2 = y1;
        }
        if (x1 == 0 || x2 == 0) {
            croak("Expresion no valida. Fila de celda no puede ser 0");
            return result;
        }
        result = getCeldas(x1, y1, x2, y2);
        return result;
    }

    /**
     * Lee una operaciÃ³n de bloque y comprueba que haya un parÃ©ntesis a continuaciÃ³n
     *
     * @return nombre de la operaciÃ³n
     */
    private String readOpBloque() {
        String nombreOp = "";
        while (!eof() && ((peek() >= 'a' && peek() <= 'z') || (peek() >= 'A' && peek() <= 'Z'))) {
            nombreOp += Character.toLowerCase(next());
        }
        readWhiteSpaces();
        if (eof() || next() != '(') {
            croak("Expresion no valida. Se espera '('");
            return nombreOp;
        }
        String[] arrayNombresOp = {
                "cortarcelda",
                "copiarcelda",
                "pegarcelda",
                "modificartipocelda",
                "modificarcolorcelda",
                "editarcontenidocelda",
                "markdownscelda",
                "ordernarcelda",
                "buscarcelda",
                "buscarreemplazar"
        };
        List<String> list = Arrays.asList(arrayNombresOp);
        if (!list.contains(nombreOp)) croak("Expresion no valida. Operacion no existe");
        return nombreOp;
    }

    /**
     * Lee un tipo de celda y comprueba que es correcto
     *
     * @return string con el tipo de celda
     */
    private String readTipoCelda() {
        String tipoCelda = readStringBloque();
        if (eof() || peek() != ')') croak("Expresion no valida. Se tiene que cerrar el parentesis");
        if ((!tipoCelda.equals("numerica")) && (!tipoCelda.equals("textual")) && (!tipoCelda.equals("fecha")) && (!tipoCelda.equals("monetaria")))
            croak("ExpresiÃ³n no vÃ¡lida. Tipo de celda no vÃ¡lido");
        return tipoCelda;
    }

    /**
     * Lee un string hasta un cierre de parÃ©ntesis, una coma o un espacio
     *
     * @return el string leÃ­do
     */
    private String readStringBloque() {
        readWhiteSpaces();
        String result = "";
        while (!eof() && peek() != ')' && peek() != ',' && peek() != ' ') {
            result += Character.toLowerCase(next());
        }
        readWhiteSpaces();
        return result;
    }

    /**
     * Lee tres doubles entre 0 i 1
     *
     * @return
     */
    private ArrayList<String> readColorCelda() {
        ArrayList<String> result = new ArrayList<String>();
        result.add(readDoubleBloque());
        if (exception) return result;
        if (Double.parseDouble(result.get(0)) > 1 || Double.parseDouble(result.get(0)) < 0) {
            croak("Expresion no valida. Valor debe ser entre 0 i 1");
            return result;
        }
        readWhiteSpaces();
        if (eof() || next() != ',') {
            croak("Expresion no valida. Se esperan 3 numeros");
            return result;
        }
        readWhiteSpaces();
        result.add(readDoubleBloque());
        if (exception) return result;
        if (Double.parseDouble(result.get(1)) > 1 || Double.parseDouble(result.get(1)) < 0) {
            croak("Expresion no valida. Valor debe ser entre 0 i 1");
            return result;
        }
        readWhiteSpaces();
        if (eof() || next() != ',') {
            croak("Expresion no valida. Se esperan 3 numeros");
            return result;
        }
        readWhiteSpaces();
        result.add(readDoubleBloque());
        if (exception) return result;
        if (Double.parseDouble(result.get(2)) > 1 || Double.parseDouble(result.get(2)) < 0) {
            croak("ExpresiÃ³n no vÃ¡lida. Valor debe ser entre 0 i 1");
            return result;
        }
        readWhiteSpaces();
        if (eof() || peek() != ')') croak("Expresion no valida. Se debe cerrar el parentesis");
        return result;
    }

    /**
     * Lee un double en formato string
     *
     * @return string del double leÃ­do
     */
    private String readDoubleBloque() {
        readWhiteSpaces();
        String result = "";
        while (!eof() && peek() >= '0' && peek() <= '9') {
            result += next();
        }
        if (!eof() && peek() == '.') result += next();
        while (!eof() && peek() >= '0' && peek() <= '9') {
            result += next();
        }
        if (result.length() == 0) croak("Expresion no valida. Se espera un numero");
        readWhiteSpaces();
        return result;
    }

    /**
     * Lee un conjunto de markdowns en formato string
     *
     * @return array de los markdowns leÃ­dos
     */
    private ArrayList<String> readMarkdowns() {
        ArrayList<String> result = new ArrayList<String>();
        String[] arrayNombresOp = {"bold", "italic"};
        List<String> list = Arrays.asList(arrayNombresOp);
        String aux = readStringBloque();
        if (!list.contains(aux)) {
            croak("Expresion no valida. No es un markdown");
            return result;
        }
        result.add(aux);
        while (!eof() && peek() == ',') {
            next();
            readWhiteSpaces();
            aux = readStringBloque();
            if (!list.contains(aux)) {
                croak("Expresion no valida. No es un markdown");
                return result;
            }
            result.add(aux);
        }
        if (eof() || peek() != ')') croak("Expresion no valida. Se debe cerrar el parentesis");
        return result;
    }

    /**
     * Lee un string entre comillas
     *
     * @return el string leÃ­do
     */
    private String readStringComillasBloque() {
        readWhiteSpaces();
        if (eof() || !(next() == '"')) {
            croak("Expresion no valida. Se esperaban comillas");
            return "";
        }
        String result = "";
        while (!eof() && ((peek() != '"') || (result.length() >= 1 && result.charAt(result.length() - 1) != '\\'))) {
            if (peek() == '"') {
                result.substring(0, result.length() - 2);
                result += '\"';
                next();
            }
            result += next();
        }
        if (eof()) croak("Expresion no valida. Se deben cerrar las comillas del string");
        return result;
    }

    /**
     * Inicializa un mapa con los diferentes tipos de moneda y su codificaciÃ³n
     *
     * @return mapa con las relaciones de tipos de monedas y sus codificaciones
     */
    private Map<TipoMoneda, String> initMonedaName() {
        Map<TipoMoneda, String> result = Stream.of(new Object[][]{
                {TipoMoneda.EUR, "EUR"},
                {TipoMoneda.USD, "USD"},
                {TipoMoneda.GBP, "GBP"},
                {TipoMoneda.CHF, "CHF"},
                {TipoMoneda.JPY, "JPY"},
                {TipoMoneda.HDK, "HDK"},
                {TipoMoneda.CAD, "CAD"},
                {TipoMoneda.CNY, "CNY"},
                {TipoMoneda.AUD, "AUD"},
                {TipoMoneda.BRL, "BRL"},
                {TipoMoneda.RUB, "RUB"},
                {TipoMoneda.MXN, "MXN"},
                {TipoMoneda.BTC, "BTC"},
                {TipoMoneda.ETH, "ETH"},
                {TipoMoneda.USDT, "USDT"},
                {TipoMoneda.BNB, "BNB"},
                {TipoMoneda.XRP, "XRP"},
                {TipoMoneda.ADA, "ADA"},
                {TipoMoneda.SOL, "SOL"},
                {TipoMoneda.LUNA, "LUNA"},
                {TipoMoneda.DOGE, "DOGE"}
        }).collect(Collectors.toMap(data -> (TipoMoneda) data[0], data -> (String) data[1]));
        return result;
    }

    /**
     * Traduce un comando para calcular el valor de un conjunto de celdas de manera
     * que el controlador de operaciones pueda ejecutar el comando
     *
     * @param comando comando que se quiere traducir
     * @return traducciÃ³n del comando en forma de Ã¡rbol
     */
    public OpTree translate(String comando) {
        input = comando;
        pos = 0;
        exception = false;
        OpTree result = new OpTree();
        if (comando.length() == 0) return result;
        readWhiteSpaces();
        if (!eof() && peek() == '=') {
            next();
            readWhiteSpaces();
            String operaciones = "+-*/";
            boolean found = false;
            Integer i, j = 0;
            for (i = 0; !found && i < 4; ++i) {
                for (j = pos; !found && j < comando.length(); ++j) {
                    if (comando.charAt(j) == operaciones.charAt(i)) {
                        found = true;
                    } else if (comando.charAt(j) == '(') j = skipParenthesis(j);
                    if (exception) {
                        result.node = new Token();
                        result.node.type = typeToken.exception;
                        result.hijos = new ArrayList<ArrayList<OpTree>>();
                        return result;
                    }
                }
            }

            if (found) {
                --j;
                --i;
                result.node = traducirSimb(operaciones.charAt(i));
                if (exception) {
                    result.node = new Token();
                    result.node.type = typeToken.exception;
                    result.hijos = new ArrayList<ArrayList<OpTree>>();
                    return result;
                }
                ArrayList<OpTree> argumentos = new ArrayList<OpTree>();
                String aux = '=' + comando.substring(1, j - 1);
                argumentos.add(translate(aux));
                if (exception) {
                    result.node = new Token();
                    result.node.type = typeToken.exception;
                    result.hijos = new ArrayList<ArrayList<OpTree>>();
                    return result;
                }
                aux = '=' + comando.substring(j + 1, comando.length() - 1);
                if (((TokenOperacion) result.node).operation == TokenOperacion.opName.add || ((TokenOperacion) result.node).operation == TokenOperacion.opName.mult) {
                    argumentos.add(translate(aux));
                    if (exception) {
                        result.node = new Token();
                        result.node.type = typeToken.exception;
                        result.hijos = new ArrayList<ArrayList<OpTree>>();
                        return result;
                    }
                    result.hijos.add(argumentos);
                } else {
                    ArrayList<OpTree> argumentos2 = new ArrayList<OpTree>();
                    argumentos2.add(translate(aux));
                    if (exception) {
                        result.node = new Token();
                        result.node.type = typeToken.exception;
                        result.hijos = new ArrayList<ArrayList<OpTree>>();
                        return result;
                    }
                    result.hijos.add(argumentos);
                    result.hijos.add(argumentos2);
                }
            } else if (!eof() && peek() == '(') {
                for (j = comando.length() - 1; !found && j > 0; --j) {
                    if (comando.charAt(j) == ')') {
                        result = translate('=' + comando.substring(pos, j - 1));
                        if (exception) {
                            result.node = new Token();
                            result.node.type = typeToken.exception;
                            result.hijos = new ArrayList<ArrayList<OpTree>>();
                            return result;
                        }
                        found = true;
                    } else if (comando.charAt(j) != ' ') {
                        croak("Expresion no valida. Debe acabar en ')'");
                        result.node = new Token();
                        result.node.type = typeToken.exception;
                        result.hijos = new ArrayList<ArrayList<OpTree>>();
                        return result;
                    }
                }
                if (!found) {
                    croak("Expresion no valida. Falta cerrar parentesis");
                    result.node = new Token();
                    result.node.type = typeToken.exception;
                    result.hijos = new ArrayList<ArrayList<OpTree>>();
                    return result;
                }
                result = translate('=' + comando.substring(pos + 1, j));
            } else result = readExp();

            if (!eof()) {
                croak("Expresion no valida. Demasiados argumentos");
                result.node = new Token();
                result.node.type = typeToken.exception;
                result.hijos = new ArrayList<ArrayList<OpTree>>();
                return result;
            }
        } else {
            result = readLiteral();
            if (exception) {
                result.node = new Token();
                result.node.type = typeToken.exception;
                result.hijos = new ArrayList<ArrayList<OpTree>>();
                return result;
            }
        }

        if (exception) {
            result.node = new Token();
            result.node.type = typeToken.exception;
            result.hijos = new ArrayList<ArrayList<OpTree>>();
            return result;
        }
        return result;
    }

    /**
     * Traduce un string que codifica un bloque de celdas y devuelve un array con los
     * Ã­ndices de todas las celdas que componene el bloque
     *
     * @param s string que codifica el bloque de celdas
     * @return lista con los Ã­ndices de las celdas codificadas
     */
    public ArrayList<ArrayList<Integer>> translateBloques(String s) {
        input = s;
        pos = 0;
        exception = false;
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
        readWhiteSpaces();
        if (!eof() && ((peek() >= 'a' && peek() <= 'z') || (peek() >= 'A' && peek() <= 'Z'))) {
            result.addAll(readBloque());
            if (exception) {
                return new ArrayList<ArrayList<Integer>>();
            }
        } else {
            croak("Bloques no validos. Debe empezar por una letra");
            return new ArrayList<ArrayList<Integer>>();
        }
        readWhiteSpaces();
        while (!eof() && String.valueOf(peek()).equals(",")) {
            next();
            readWhiteSpaces();
            result.addAll(readBloque());
            if (exception) {
                return new ArrayList<ArrayList<Integer>>();
            }
            readWhiteSpaces();
        }
        if (!eof()) {
            croak("Bloques no validos");
            return new ArrayList<ArrayList<Integer>>();
        }
        return result;
    }

    /**
     * Traduce un string que codifica una operaciÃ³n de bloque
     *
     * @param s codificaciÃ³n de la operaciÃ³n
     * @return array con los elementos de la traducciÃ³n
     */
    public ArrayList<String> translateOpBloque(String s) {
        input = s;
        pos = 0;
        exception = false;
        ArrayList<String> result = new ArrayList<String>();
        readWhiteSpaces();
        result.add(readOpBloque());
        if (exception) {
            return new ArrayList<String>();
        }
        readWhiteSpaces();
        switch (result.get(0)) {
            case "modificartipocelda":
                result.add(readTipoCelda());
                break;
            case "modificarcolorcelda":
                result.addAll(readColorCelda());
                break;
            case "editarcontenidocelda":
                result.add(readStringComillasBloque());
                if (eof() || next() != ')') croak("Expresion no valida. Se debe cerrar el parentesis");
                break;
            case "markdownscelda":
                result.addAll(readMarkdowns());
                break;
            case "buscarcelda":
                result.add(readStringComillasBloque());
                if (eof() || next() != ')') croak("Expresion no valida. Se debe cerrar el parentesis");
                break;
            case "buscarreemplazar":
                result.add(readStringComillasBloque());
                if (eof() || next() != ',') croak("Expresion no valida. Se espera un segundo argumento");
                readWhiteSpaces();
                result.add(readStringBloque());
                if (eof() || next() != ')') croak("Expresion no valida. Se debe cerrar el parentesis");
                break;
            default:
                if (eof() || next() != ')') croak("Expresion no valida. Se debe cerrar el parentesis");
                break;
        }
        if (exception) {
            return new ArrayList<String>();
        }
        return result;
    }

    /**
     * Dada un string que representa una moneda, devuelve la codificaciÃ³n estÃ¡ndar
     * que el programa usa para esa moneda
     *
     * @param s representa la moneda
     * @return codificaciÃ³n estÃ¡ndar de la moneda en el programa
     */
    public String convertirTipoMoneda(String s) {
        Map<String, TipoMoneda> mapMonedas;
        mapMonedas = initializeCoinMap();
        if (!mapMonedas.containsKey(s.toUpperCase())) croak("No es un tipo de moneda");
        if (exception) return "exception";
        Map<TipoMoneda, String> mapNombresMonedas;
        mapNombresMonedas = initMonedaName();
        return mapNombresMonedas.get(mapMonedas.get(s));
    }
}
