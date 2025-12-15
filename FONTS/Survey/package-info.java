/**
 * <p>El package {@code Survey} centralitza el model de domini de les enquestes
 * utilitzat per la terminal CLI.</p>
 *
 * <p>Inclou:</p>
 * <ul>
 *   <li>{@link Survey.Survey Survey}: contenidor principal amb la capçalera de
 *       l'enquesta, la configuració de clustering i el catàleg de preguntes.</li>
 *   <li>{@link Survey.Question Question} i els seus subtipus
 *       ({@link Survey.OpenStringQuestion}, {@link Survey.OpenIntQuestion},
 *       {@link Survey.SingleChoiceQuestion}, {@link Survey.MultipleChoiceQuestion})
 *       que defineixen els formats de resposta acceptats.</li>
 *   <li>{@link Survey.ChoiceOption ChoiceOption}: representació de cada opció
 *       seleccionable en preguntes de tipus "single" o "multi".</li>
 *   <li>{@link Survey.LocalPersistence LocalPersistence}: emmagatzematge en memòria
 *       utilitzat pels controladors per guardar i recuperar enquestes i respostes
 *       durant les sessions locals.</li>
 * </ul>
 *
 * <p>Aquest package actua com a punt d'entrada per compartir l'estat de les
 * enquestes entre controladors, serveis d'import/export i la capa de
 * presentació.</p>
 */
package Survey;